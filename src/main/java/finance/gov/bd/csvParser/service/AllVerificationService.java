package finance.gov.bd.csvParser.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import finance.gov.bd.csvParser.dto.request.VerificationRequest;
import finance.gov.bd.csvParser.dto.response.*;
import finance.gov.bd.csvParser.model.BrnVerificationLog;
import finance.gov.bd.csvParser.model.NidVerificationLog;
import finance.gov.bd.csvParser.repository.BrnVerificationLogRepo;
import finance.gov.bd.csvParser.repository.NidVerificationLogRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

@Service
public class AllVerificationService {
    private static String urlStr = "https://api.finance.gov.bd/";
    private static String token = "";
    private GsonBuilder builder = new GsonBuilder();
    private Gson gson = builder.create();

    @Autowired
    NidVerificationLogRepo nidVerificationLogRepo;

    @Autowired
    BrnVerificationLogRepo brnVerificationLogRepo;

    public ServiceResponse verify(VerificationRequest req) {
        try {
            if (req.getCheckBy() != null) {
                if (req.getCheckBy().equals("N")) {
                    verifyNid(req);
                } else if (req.getCheckBy().equals("B")) {
                    verifyBrn(req);
                }
            }
            return new ServiceResponse();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private void verifyNid(VerificationRequest req) {
        Integer size = req.getSize() != null ? req.getSize() : 1000;
        List<NidVerificationLog> list = nidVerificationLogRepo.findByNidVerifyStatus(req.getVerifyStatus(), size);

        List<NidVerificationLog> verifyList = new ArrayList<>();

        if (list != null && list.size() > 0) {
            for (NidVerificationLog dto : list) {
                if (dto.getNid() != null && dto.getDateOfBirth() != null) {
                    try {
                        BECResponse response = getNidData(dto.getNid().toString(), dto.getDateOfBirth().toString());
                        if (response != null) {
                            if (response.isOperationResult() && response.getNidData() != null) {
                                dto.setNidVerifyStatus(1);
                                dto.setNameEn(response.getNidData().getNameEn());
                                dto.setNameBn(response.getNidData().getName());
                                String nid10 = response.getNidData().getNid10();
                                String nid17 = response.getNidData().getNid17();
                                if (nid10 != null && dto.getNid().toString().equals(nid10)) {
                                    dto.setAlternateNid(new BigInteger(nid17));
                                } else if (nid17 != null && dto.getNid().toString().equals(nid17)) {
                                    dto.setAlternateNid(new BigInteger(nid10));
                                }
                            } else {
                                dto.setNidVerifyStatus(2);
                            }
                        } else {
                            dto.setNidVerifyStatus(3);
                        }

                        dto.setLastVerifyAt(new java.util.Date());
                        verifyList.add(dto);

                    } catch (Exception ex) {
                        dto.setNidVerifyStatus(3);
                        dto.setLastVerifyAt(new java.util.Date());
                        verifyList.add(dto);
                        ex.printStackTrace();
                    }
                }
            }
        }

        if (verifyList.size() > 0) {
            nidVerificationLogRepo.saveAll(verifyList);
        }
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
                            if (response.personname != null || response.personname.isEmpty() || response.personnameEn != null || response.personnameEn.isEmpty()) {
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

    private synchronized String getToken() {
        try {
            if (token != null && !token.isEmpty()) {
                return token;
            }
            System.out.println("Get Token Call");

            URL url = new URL(urlStr + "ibas2api/api/token");
            System.out.println("Toke Url" + urlStr + "ibas2api/api/token");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            con.setRequestProperty("charset", "utf-8");
            con.setConnectTimeout(30000);
            con.setReadTimeout(30000);
            con.setDoOutput(true);
            con.setDoInput(true);
            String userpass = "HSP:HSPA2D0183A-AD55-4571-AB5D-C4294A6FE7DF";
            String urlParameters = "username=" + "hsp" + "&password=" + "bangladesh" + "&grant_type=password";
            String encoded = Base64.getEncoder().encodeToString((userpass).getBytes(StandardCharsets.UTF_8));
            con.setRequestProperty("Authorization", "Basic " + encoded);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();

            int responseCode = con.getResponseCode();

            System.out.println("Reponse Code: " + responseCode);

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

    private BECResponse getNidData(String nid, String dob) {
        JSONObject becJsonResult = null;

        try {
            if (token.equals("")) {
                getToken();
            }
            System.out.println("Token :------------>" + token);
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
                System.out.println(jsonResponse);
            }

            NidResponse nidResponse = gson.fromJson(becJsonResult.toString(), NidResponse.class);
            System.out.println("NID Data from Ibas " + becJsonResult.toString());
            BECResponse becResponse = getBECResponseEC(nidResponse);
            if (nidResponse != null && nidResponse.getStatusCode().equals("SUCCESS")) {
            }
            return becResponse;

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
            System.out.println("brn: " + bRNReponse.getUbrn());
            System.out.println("dob: " + bRNReponse.getDob());

            return bRNReponse;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private BRNResponse getBRNdata(String brn, String dob) {
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
            System.out.println("Reponse Code: " + responseCode);
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
                    System.out.println("Response Code: " + responseCode2);
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

}