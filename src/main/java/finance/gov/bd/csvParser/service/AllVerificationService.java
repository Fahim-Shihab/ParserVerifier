package finance.gov.bd.csvParser.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import finance.gov.bd.csvParser.common.Data;
import finance.gov.bd.csvParser.dto.request.VerificationRequest;
import finance.gov.bd.csvParser.dto.response.*;
import finance.gov.bd.csvParser.model.BrnVerificationLog;
import finance.gov.bd.csvParser.model.MfsVerificationLog;
import finance.gov.bd.csvParser.repository.*;
import finance.gov.bd.csvParser.threads.AllVerifyThread;
import finance.gov.bd.csvParser.threads.NidVerifyThread;
import finance.gov.bd.csvParser.threads.MfsVerifyThread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONException;
import org.json.JSONObject;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;

@Service
public class AllVerificationService {
    private static String urlStr = "https://api.finance.gov.bd/";
    private static String token = "";
    private GsonBuilder builder = new GsonBuilder();
    private Gson gson = builder.create();

    @Autowired
    EntityManager em;

    @Autowired
    NidVerificationLogRepo nidVerificationLogRepo;

    @Autowired
    BrnVerificationLogRepo brnVerificationLogRepo;

    @Autowired
    MfsVerificationLogRepo mfsVerificationLogRepo;

    @Autowired
    SrNeedVerifyRepo srNeedVerifyRepo;

    int verifyThread = 200;

    @Autowired
    NidInfoRepo nidInfoRepo;

    @Autowired
    BirthRegInfoRepo birthRegInfoRepo;

    @Value("${ibasapi.username}")
    String ibasApiUsername;

    @Value("${ibasapi.password}")
    String ibasApiPassword;

    @Value("${ibasapi.userpass}")
    String ibasApiUserpass;

    private void nidVerifyThreads(VerificationRequest req) {
        List<Object[]> result = em.createNativeQuery("select min(id) min, max(id) max from " +
                " nid_verification_log where nid_verify_status="+req.getVerifyStatus()).getResultList();

        Integer minId = -1;
        Integer maxId = -1;

        if (result != null && !result.isEmpty() && result.size() == 2) {
            for (Object[] obj : result) {
                minId = Integer.parseInt(obj[0] + "");
                maxId = Integer.parseInt(obj[1] + "");
            }
        } else {
            return;
        }

        Integer offset = minId;

        if (minId != -1 && maxId != -1) {
            Integer threadCount = maxId / 1000;

            NidVerifyThread[] threads = new NidVerifyThread[threadCount+1];
            for (int i=0; i<threadCount+1; i++) {
                threads[i] = new NidVerifyThread("Thread-"+(i+1), offset, nidVerificationLogRepo, this);
                offset += 1000;
            }

            ExecutorService pool = Executors.newCachedThreadPool();
            for (int i=0; i<threadCount+1; i++) {
                pool.execute(threads[i]);
            }
            pool.shutdown();
        }
    }

    private void mfsVerifyThreads(VerificationRequest req) {
        List<Object[]> result = em.createNativeQuery("select min(id) min, max(id) max from " +
                " mfs_verification_log where mfs_verify_status="+req.getVerifyStatus()).getResultList();

        Integer minId = -1;
        Integer maxId = -1;

        if (result != null && !result.isEmpty() && result.size() == 2) {
            for (Object[] obj : result) {
                minId = Integer.parseInt(obj[0] + "");
                maxId = Integer.parseInt(obj[1] + "");
            }
        } else {
            return;
        }

        Integer offset = minId;

        if (minId != -1 && maxId != -1) {
            Integer threadCount = maxId / 1000;

            MfsVerifyThread[] threads = new MfsVerifyThread[threadCount+1];
            for (int i=0; i<threadCount+1; i++) {
                threads[i] = new MfsVerifyThread("Thread-"+(i+1), offset, mfsVerificationLogRepo, this);
                offset += 1000;
            }

            ExecutorService pool = Executors.newCachedThreadPool();
            for (int i=0; i<threadCount+1; i++) {
                pool.execute(threads[i]);
            }
            pool.shutdown();
        }
    }

    @Scheduled(initialDelay = 5 * 1000, fixedDelay = 24 * 60 * 60 * 1000)
    public void startVerify() {
        System.out.println("Start");
        verifyThreadsV1();
    }

    public ServiceResponse verify(VerificationRequest req) {
        try {
            if (req.getCheckBy() != null) {
                if (req.getCheckBy().equals("N")) {
//                    verifyNid(req);
                    nidVerifyThreads(req);
                } else if (req.getCheckBy().equals("B")) {
                    verifyBrn(req);
                } else if (req.getCheckBy().equals("M")) {
//                    verifyMfs(req);
                    mfsVerifyThreads(req);
                } else if (req.getCheckBy().equals("A")) {
                    verifyThreadsV1();
                }
            }
            return new ServiceResponse();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private void verifyThreadsV1() {
        long nidVerifyRequiredCount = srNeedVerifyRepo.countVerificationPending();
        if (nidVerifyRequiredCount == 0) {
            System.out.println("No data to verify. Exiting");
            return;
        }
        Double r = (double) (nidVerifyRequiredCount / verifyThread);
        long recordsPerThread = r.longValue() + 1;

        AllVerifyThread[] threads = new AllVerifyThread[verifyThread];
        ExecutorService pool = Executors.newCachedThreadPool();
        Long currentOffset = 0L;
        for (int i=0; i < verifyThread; i++) {
            threads[i] = new AllVerifyThread("Thread-"+(i+1), currentOffset, recordsPerThread,
                    srNeedVerifyRepo, nidInfoRepo, birthRegInfoRepo, this, em);
            currentOffset += recordsPerThread;
            pool.execute(threads[i]);
        }
        pool.shutdown();
    }

    private void verifyBrn(VerificationRequest req) {
        Integer size = req.getSize() != null ? req.getSize() : 1000;
        List<BrnVerificationLog> list = brnVerificationLogRepo.findByBrnVerifyStatus(req.getVerifyStatus(), size);

        List<BrnVerificationLog> verifyList = new ArrayList<>();

        if (list != null && list.size() > 0) {
            for (BrnVerificationLog dto : list) {
                if (dto.getBirthRegNo() != null && dto.getDateOfBirth() != null) {
                    try {
                        BRNResponse response = getBRNdata(dto.getBirthRegNo().toString(), dto.getDateOfBirth().toString());
                        if (response != null) {
                            if (response.personname != null || response.personnameEn != null) {
                                dto.setBrnVerifyStatus(1);
                                dto.setNameEn(response.personnameEn);
                                dto.setNameBn(response.personname);
                            } else {
                                dto.setBrnVerifyStatus(2);
                            }
                        } else {
                            dto.setBrnVerifyStatus(3);
                        }

                        dto.setLastVerifyAt(new java.util.Date());
                        verifyList.add(dto);

                    } catch (Exception ex) {
                        dto.setBrnVerifyStatus(3);
                        dto.setLastVerifyAt(new java.util.Date());
                        verifyList.add(dto);
                        ex.printStackTrace();
                    }
                }
            }
        }

        if (verifyList.size() > 0) {
            brnVerificationLogRepo.saveAll(verifyList);
        }
    }

    private void verifyMfs(VerificationRequest req) {
        Integer size = req.getSize() != null ? req.getSize() : 100;

        long count = mfsVerificationLogRepo.countByNidVerifyStatus(0);

        int index = 0;

        while (index < count) {

            List<MfsVerificationLog> list = mfsVerificationLogRepo.findByMfsVerifyStatus(req.getVerifyStatus(), size);

            List<MfsVerificationLog> verifyList = new ArrayList<>();

            if (list != null && list.size() > 0) {
                for (MfsVerificationLog dto : list) {
                    if (dto.getNid() != null && dto.getMfsName() != null && dto.getMobileNumber() != null) {
                        try {
                            MFSResponse response = getMFSData(dto.getMfsName().toLowerCase(), dto.getNid().toString(), dto.getMobileNumber());
                            if (response != null) {
                                if (response.getAccountExist() == false || response.getNIDMatched() == false || response.getNIDMobileMatched() == false) {
                                    if (dto.getAlternateNid() != null) {
                                        MFSResponse response2 = getMFSData(dto.getMfsName().toLowerCase(), dto.getAlternateNid().toString(), dto.getMobileNumber());
                                        if (response2 != null) {
                                            if (response2.getAccountExist() != null && response2.getAccountExist() == true && response.getAccountExist() == false) {
                                                response.setAccountExist(true);
                                            }
                                            if (response2.getNIDMatched() != null && response2.getNIDMatched() == true && response.getNIDMatched() == false) {
                                                response.setNIDMatched(true);
                                            }
                                            if (response2.getNIDMobileMatched() != null && response2.getNIDMobileMatched() == true && response.getNIDMobileMatched() == false) {
                                                response.setNIDMobileMatched(true);
                                            }
                                        }
                                    }
                                }
                                dto = updateMfsAccountInfo(dto, response);
                            } else {
                                dto.setAccountExist(3);
                                dto.setNidMatched(3);
                                dto.setNidMobileMatched(3);
                                dto.setMfsVerifyStatus(3);
                            }

                            dto.setLastVerifyAt(new java.util.Date());
                            verifyList.add(dto);

                        } catch (Exception ex) {
                            dto.setMfsVerifyStatus(3);
                            dto.setLastVerifyAt(new java.util.Date());
                            verifyList.add(dto);
                            ex.printStackTrace();
                        }
                    }
                }
            }

            if (verifyList.size() > 0) {
                mfsVerificationLogRepo.saveAll(verifyList);
            }

            index += size;
        }
    }

    public MfsVerificationLog updateMfsAccountInfo(MfsVerificationLog dto, MFSResponse response) {
        if (response.getAccountExist() != null) {
            if (response.getAccountExist())   dto.setAccountExist(1);
            else    dto.setAccountExist(2);
        }
        else {
            dto.setAccountExist(3);
        }
        if (response.getNIDMatched() != null) {
            if (response.getNIDMatched())   dto.setNidMatched(1);
            else    dto.setNidMatched(2);
        }
        else {
            dto.setNidMatched(3);
        }
        if (response.getNIDMobileMatched() != null) {
            if (response.getNIDMobileMatched())   dto.setNidMobileMatched(1);
            else    dto.setNidMobileMatched(2);
        }
        else {
            dto.setNidMobileMatched(3);
        }
        dto.setMfsVerifyStatus(1);
        return dto;
    }

    private synchronized String getToken() {
        try {
            if (token != null && !token.isEmpty()) {
                return token;
            }
            System.out.println("Get Token Call");

            URL url = new URL(urlStr + "ibas2api/api/token");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            con.setRequestProperty("charset", "utf-8");
            con.setConnectTimeout(30000);
            con.setReadTimeout(30000);
            con.setDoOutput(true);
            con.setDoInput(true);
            String userpass = ibasApiUserpass;
            String urlParameters = "username=" + ibasApiUsername + "&password=" + ibasApiPassword + "&grant_type=password";
            String encoded = Base64.getEncoder().encodeToString((userpass).getBytes(StandardCharsets.UTF_8));
            con.setRequestProperty("Authorization", "Basic " + encoded);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();

            int responseCode = con.getResponseCode();

            System.out.println("Token Response Code: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                JSONObject jsonResponse = new JSONObject(response.toString());
                System.out.println("op result = " + jsonResponse);

                System.out.println(jsonResponse);

                AuthorizationResponse authReponse = gson.fromJson(response.toString(), AuthorizationResponse.class);

                token = authReponse.getAccess_token();
                System.out.println("Token: " + token);
                return "Success";

            } else {
                System.out.println("POST request error : " + responseCode);
                return "Failed";
            }
        } catch (Exception ex) {
            return "Failed";
        }
    }

    public Data getNidData(String nid, String dob) {
        JSONObject becJsonResult = null;

        try {
            if (token.equals("")) {
                getToken();
            }
            URL url = new URL(urlStr + "ibas2api/api/saftynet/NIDInfo?nid=" + nid + "&dob=" + dob);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Accept-Charset", "UTF-8");
            con.setRequestProperty("Authorization", "Bearer " + token);
            con.setConnectTimeout(30000);
            con.setReadTimeout(30000);

            int responseCode = con.getResponseCode();
            System.out.println("Response Code: " + responseCode);
            if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED || responseCode == 400 || responseCode == 302) {
                token = "";
                getToken();
            }
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        con.getInputStream(), "utf-8"));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                JSONObject jsonResponse = new JSONObject(response.toString());
                becJsonResult = jsonResponse;
            }

            NidResponse nidResponse = gson.fromJson(becJsonResult.toString(), NidResponse.class);
            System.out.println("NID Data from Ibas " + becJsonResult.toString());
//            BECResponse becResponse = getBECResponseEC(nidResponse);
//            if (nidResponse != null && nidResponse.getStatusCode().equals("SUCCESS")) {
//            }
            if (nidResponse.getSuccess() != null && nidResponse.getSuccess().getData() != null) {
                return nidResponse.getSuccess().getData();
            } else {
                return new Data();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    private BECResponse getBECResponseEC(NidResponse becJsonResult) throws JSONException {
        BECResponse bECResponse = new BECResponse();
        String language = LocaleContextHolder.getLocale().getLanguage();
        if (becJsonResult.getStatusCode() != null && becJsonResult.getStatusCode().equals("SUCCESS")) {
            System.out.println("SUCCESS --------------------------");
            bECResponse.setMatchFound(true);
            bECResponse.setOperationResult(true);
            BECNidResponse nidData = new BECNidResponse();

            nidData.setName(becJsonResult.getSuccess().getData().getName());
            nidData.setNameEn(becJsonResult.getSuccess().getData().getNameEn());
            nidData.setNid(becJsonResult.getSuccess().getData().getNationalId());
            if (becJsonResult.getSuccess().getData().getNationalId() != null && becJsonResult.getSuccess().getData().getNationalId().length() == 10) {
                nidData.setNid10(becJsonResult.getSuccess().getData().getNationalId());
            } else if (becJsonResult.getSuccess().getData().getNationalId() != null && becJsonResult.getSuccess().getData().getNationalId().length() == 17) {
                nidData.setNid17(becJsonResult.getSuccess().getData().getNationalId());
            }
            if (becJsonResult.getSuccess().getData().getPin() != null && becJsonResult.getSuccess().getData().getPin().length() == 10) {
                nidData.setNid10(becJsonResult.getSuccess().getData().getPin());
            } else if (becJsonResult.getSuccess().getData().getPin() != null && becJsonResult.getSuccess().getData().getPin().length() == 17) {
                nidData.setNid17(becJsonResult.getSuccess().getData().getPin());
            }
            nidData.setPin(becJsonResult.getSuccess().getData().getPin());
            nidData.setGender(becJsonResult.getSuccess().getData().getGender());
            nidData.setMother(becJsonResult.getSuccess().getData().getMother());
            nidData.setFather(becJsonResult.getSuccess().getData().getFather());
            bECResponse.setNidData(nidData);

        } else {
            bECResponse.setMatchFound(false);
            bECResponse.setErrorMsg(language == "en" ? "NID information Not found" : "জাতীয়পরিচয় পত্রের তথ্য খুঁজে পাওয়া যায়নি। পুনরায় চেষ্টা করুন। ");
            bECResponse.setErrorCode("400");

            System.out.println("match not found");
        }
        return bECResponse;
    }

    private BRNResponse processBRNResponse(InputStream inputStream) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            JSONObject jsonResponse = new JSONObject(response.toString());

            System.out.println(jsonResponse);

            BRNResponse bRNReponse = gson.fromJson(response.toString(), BRNResponse.class);
//            System.out.println("brn: " + bRNReponse.getUbrn());
//            System.out.println("dob: " + bRNReponse.getDob());

            return bRNReponse;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public BRNResponse getBRNdata(String brn, String dob) {
        try {
            if (token.equals("")) {
                getToken();
            }
            URL url = new URL(urlStr + "ibas2api/api/saftynet/BRNInfo?ubrn=" + brn + "&dob=" + dob);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Accept-Charset", "UTF-8");
            con.setRequestProperty("Authorization", "Bearer " + token);
            con.setConnectTimeout(30000);
            con.setReadTimeout(30000);

            int responseCode = con.getResponseCode();
//            System.out.println("BRN Response Code: " + responseCode);
            if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                token = "";
                getToken();
                if (token != null && !token.isEmpty()) {
                    HttpURLConnection con2 = (HttpURLConnection) url.openConnection();
                    con2.setRequestMethod("GET");
                    con2.setRequestProperty("Accept-Charset", "UTF-8");
                    con2.setRequestProperty("Authorization", "Bearer " + token);
                    con2.setConnectTimeout(30000);
                    con2.setReadTimeout(30000);

                    int responseCode2 = con2.getResponseCode();
//                    System.out.println("BRN Response Code: " + responseCode2);
                    if (responseCode2 == HttpURLConnection.HTTP_OK) {
                        return processBRNResponse(con2.getInputStream());
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            } else if (responseCode == HttpURLConnection.HTTP_OK) {
                return processBRNResponse(con.getInputStream());
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
        return null;
    }

    private MFSResponse getMFSResponseAfterFirstTokenExpiry(URL url) {
        try {
            getToken();
            if (token != null && !token.isEmpty()) {
                HttpURLConnection con2 = (HttpURLConnection) url.openConnection();
                con2.setRequestMethod("GET");
                con2.setRequestProperty("Accept-Charset", "UTF-8");
                con2.setRequestProperty("Authorization", "Bearer " + token);
                con2.setConnectTimeout(30000);
                con2.setReadTimeout(30000);

                int responseCode2 = con2.getResponseCode();
                System.out.println("Response Code: " + responseCode2);
                if (responseCode2 == HttpURLConnection.HTTP_OK) {
                    BufferedReader in2 = new BufferedReader(new InputStreamReader(
                            con2.getInputStream(), "utf-8"));
                    String inputLine2;
                    StringBuffer response2 = new StringBuffer();

                    while ((inputLine2 = in2.readLine()) != null) {
                        response2.append(inputLine2);
                    }
                    JSONObject jsonResponse2 = new JSONObject(response2.toString());

                    System.out.println(jsonResponse2);
                    MFSResponse mFSResponse = gson.fromJson(response2.toString(), MFSResponse.class);
                    System.out.println("msg: " + mFSResponse.getMessage());
                    System.out.println("account: " + mFSResponse.getAccountExist());
                    System.out.println("nid: " + mFSResponse.getNIDMatched());
                    System.out.println("nid-mobile: " + mFSResponse.getNIDMobileMatched());
                    mFSResponse.setErrorCode(200);
                    return mFSResponse;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public MFSResponse getMFSData(String mfs, String nid, String mobile) {
        String language = LocaleContextHolder.getLocale().getLanguage();

        if (token == null || token.isEmpty()) {
            getToken();
        }
        if (token != null && !token.isEmpty()) {
            try {

                URL url = new URL(urlStr + "ibas2api/api/saftynet/MFSInfo?mfs=" + mfs + "&nid=" + nid + "&mobile=" + mobile);
                System.out.println("URL : " + urlStr + "ibas2api/api/saftynet/MFSInfo?mfs=" + mfs + "&nid=" + nid + "&mobile=" + mobile);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setRequestProperty("Accept-Charset", "UTF-8");
                con.setRequestProperty("Authorization", "Bearer " + token);
                con.setConnectTimeout(30000);
                con.setReadTimeout(30000);

                int responseCode = con.getResponseCode();
                System.out.println("Response Code: " + responseCode);
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(
                            con.getInputStream(), "utf-8"));
                    String inputLine;
                    StringBuffer response = new StringBuffer();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    JSONObject jsonResponse = new JSONObject(response.toString());

                    System.out.println(jsonResponse);

                    if (response.toString().equals("{\"responseCode\":\"1\"}")) {
                        System.out.println("Expired Token");
                        token = "";
                        return getMFSResponseAfterFirstTokenExpiry(url);
                    } else {
                        MFSResponse mFSResponse = gson.fromJson(response.toString(), MFSResponse.class);
                        System.out.println("msg: " + mFSResponse.getMessage());
                        System.out.println("account: " + mFSResponse.getAccountExist());
                        System.out.println("nid: " + mFSResponse.getNIDMatched());
                        System.out.println("nid-mobile: " + mFSResponse.getNIDMobileMatched());
                        mFSResponse.setErrorCode(200);
                        return mFSResponse;
                    }
                } else if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    System.out.println("Expired Token. Now HTTP response code is correct in MFS verification");
                    token = "";
                    return getMFSResponseAfterFirstTokenExpiry(url);
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(e.getMessage());

            }
        }
        MFSResponse mFSResponse = new MFSResponse();
        mFSResponse.setAccountExist(false);
        mFSResponse.setMessage(("en".equals(language) ? "Server Issue" : "সার্ভার এর সাথে যোগাযোগ করা সম্ভব হয়নি। কিছুক্ষন পর আবার চেষ্টা করুন"));
        mFSResponse.setErrorCode(500);
        return mFSResponse;
    }

}
