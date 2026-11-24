package com.suraj.Customer_Portal_29.entity;

public enum PdfDocType {

    WCR_Undertaking_Guarantee_Aadhar(10),
    Annexure_I_Proforma_A(10),
    Declaration_FOR_DCR(10),
    NET_METERING_CONNECTION_AGREEMENT(10),
    SOLAR_PROPOSAL_11KW(10),
    METER_TESTING_REQUEST(10),
    PM_SURYA_GHAR_AGREEMENT(10),
    FrontSix(10),
    Site_photos(10),

    ALL_IN_ONE(50),
    ALL_NINE(80);

    private final int costInRupees;

    PdfDocType(int costInRupees) {
        this.costInRupees = costInRupees;
    }

    public int getCostInRupees() {
        return costInRupees;
    }

    public static PdfDocType fromUrlSegment(String segment) {
        if (segment == null) return null;
        switch (segment) {
            case "WCR_Undertaking_Guarantee_Aadhar": return WCR_Undertaking_Guarantee_Aadhar;
            case "Annexure-I_Proforma-A":             return Annexure_I_Proforma_A;
            case "Declaration_FOR_DCR":               return Declaration_FOR_DCR;
            case "NET_METERING_CONNECTION_AGREEMENT": return NET_METERING_CONNECTION_AGREEMENT;
            case "SOLAR_PROPOSAL_11KW":               return SOLAR_PROPOSAL_11KW;
            case "METER_TESTING_REQUEST":             return METER_TESTING_REQUEST;
            case "PM_SURYA_GHAR_AGREEMENT":           return PM_SURYA_GHAR_AGREEMENT;
            case "FrontSix":                          return FrontSix;
            case "site-photos":                       return Site_photos;
            case "all-in-one":                        return ALL_IN_ONE;
            case "all-nine":                          return ALL_NINE;
            default:                                  return null;
        }
    }

    public String toUrlSegment() {
        switch (this) {
            case WCR_Undertaking_Guarantee_Aadhar:    return "WCR_Undertaking_Guarantee_Aadhar";
            case Annexure_I_Proforma_A:               return "Annexure-I_Proforma-A";
            case Declaration_FOR_DCR:                 return "Declaration_FOR_DCR";
            case NET_METERING_CONNECTION_AGREEMENT:   return "NET_METERING_CONNECTION_AGREEMENT";
            case SOLAR_PROPOSAL_11KW:                 return "SOLAR_PROPOSAL_11KW";
            case METER_TESTING_REQUEST:               return "METER_TESTING_REQUEST";
            case PM_SURYA_GHAR_AGREEMENT:             return "PM_SURYA_GHAR_AGREEMENT";
            case FrontSix:                            return "FrontSix";
            case Site_photos:                         return "site-photos";
            case ALL_IN_ONE:                          return "all-in-one";
            case ALL_NINE:                            return "all-nine";
            default:                                  return name();
        }
    }
}