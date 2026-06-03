package com.stepcore.business.earnings.model;

public record ClassifiedMinutes(
        int normal,
        int daytimeOt,
        int nightSurcharge,
        int nocturnalOt
) {
    public static final ClassifiedMinutes ZERO = new ClassifiedMinutes(0, 0, 0, 0);

    public int totalBillableMinutes() {
        return normal + daytimeOt + nightSurcharge + nocturnalOt;
    }

    public ClassifiedMinutes capped(final int maxNormalMinutes, final int maxDaytimeOtMinutes) {
        return new ClassifiedMinutes(
                Math.min(normal, maxNormalMinutes),
                Math.min(daytimeOt, maxDaytimeOtMinutes),
                0,
                0);
    }
}
