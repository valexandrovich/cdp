package ua.com.valexa.cdpcommon.enums;

public enum StateMapping {
    AL("ALABAMA"),
    AK("ALASKA"),
    AZ("ARIZONA"),
    AR("ARKANSAS"),
    CA("CALIFORNIA"),
    CO("COLORADO"),
    CT("CONNECTICUT"),
    DE("DELAWARE"),
    FL("FLORIDA"),
    GA("GEORGIA"),
    HI("HAWAII"),
    ID("IDAHO"),
    IL("ILLINOIS"),
    IN("INDIANA"),
    IA("IOWA"),
    KS("KANSAS"),
    KY("KENTUCKY"),
    LA("LOUISIANA"),
    ME("MAINE"),
    MD("MARYLAND"),
    MA("MASSACHUSETTS"),
    MI("MICHIGAN"),
    MN("MINNESOTA"),
    MS("MISSISSIPPI"),
    MO("MISSOURI"),
    MT("MONTANA"),
    NE("NEBRASKA"),
    NV("NEVADA"),
    NH("NEW HAMPSHIRE"),
    NJ("NEW JERSEY"),
    NM("NEW MEXICO"),
    NY("NEW YORK"),
    NC("NORTH CAROLINA"),
    ND("NORTH DAKOTA"),
    OH("OHIO"),
    OK("OKLAHOMA"),
    OR("OREGON"),
    PA("PENNSYLVANIA"),
    RI("RHODE ISLAND"),
    SC("SOUTH CAROLINA"),
    SD("SOUTH DAKOTA"),
    TN("TENNESSEE"),
    TX("TEXAS"),
    UT("UTAH"),
    VT("VERMONT"),
    VA("VIRGINIA"),
    WA("WASHINGTON"),
    WV("WEST VIRGINIA"),
    WI("WISCONSIN"),
    WY("WYOMING");

    private final String state;

    StateMapping(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }

    public static StateMapping getByState(String state) {
        for (StateMapping item : values()) {
            if (item.getState().equalsIgnoreCase(state)) {
                return item;
            }
        }
        return null;
    }
}
