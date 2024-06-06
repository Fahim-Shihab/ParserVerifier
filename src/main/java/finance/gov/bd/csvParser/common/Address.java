package finance.gov.bd.csvParser.common;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Address {
    private String division;
    private String district;
    private String rmo;
    private String upozila;
    private String cityCorporationOrMunicipality;
    private String unionOrWard;
    private String postOffice;
    private String postalCode;
    private Object wardForUnionPorishod;
    private String additionalMouzaOrMoholla;
    private String additionalVillageOrRoad;
    private String homeOrHoldingNo;
    private String region;
}
