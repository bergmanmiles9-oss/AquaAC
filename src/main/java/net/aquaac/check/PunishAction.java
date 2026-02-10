package net.aquaac.check;

public enum PunishAction {
    NONE,
    ALERT,
    SETBACK,
    KICK,
    BAN;

    public static PunishAction from(String s) {
        if (s == null) return ALERT;
        try {
            return PunishAction.valueOf(s.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return ALERT;
        }
    }
}
