package uk.gov.defra.datareturns.data.model.catches;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.PrePersist;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Store the mass of a given catch
 *
 * @author Sam Gardner-Dell
 */
@Embeddable
@Getter
@Setter
public class CatchMass {
    public static final int PRECISION = 12;
    public static final int SCALE = 6;

    /**
     * Conversion factor to convert between kg/oz
     */
    private static final BigDecimal CONVERSION = BigDecimal.valueOf(0.028349523125d);

    /**
     * The type of measurement provided by the end user
     */
    @Enumerated(EnumType.STRING)
    private MeasurementType type;

    /**
     * The mass of the catch in metric kg
     */
    @Column(precision = PRECISION, scale = SCALE)
    private BigDecimal kg;

    /**
     * The mass of the catch in imperial ounces
     */
    @Column(precision = PRECISION, scale = SCALE)
    private BigDecimal oz;

    /**
     * Set the mass based on the given {@link MeasurementType} and mass
     *
     * @param measurementType the type of measurement provided by the end user
     * @param mass            the measurement given by the end user for the given measurement type
     */
    public void set(final MeasurementType measurementType, final BigDecimal mass) {
        this.type = measurementType;
        if (MeasurementType.IMPERIAL.equals(this.type)) {
            this.oz = mass;
        } else if (MeasurementType.METRIC.equals(this.type)) {
            this.kg = mass;
        }
    }

    /**
     * Conciliates the metric and imperial values prior to persisting the data into the database
     */
    @PrePersist
    public void conciliateMass() {
        if (MeasurementType.IMPERIAL.equals(this.type)) {
            // Populate the mass in kg from the imperial value
            this.kg = this.oz.multiply(CONVERSION);
        } else if (MeasurementType.METRIC.equals(this.type)) {
            this.oz = this.kg.divide(CONVERSION, SCALE, RoundingMode.HALF_UP);
        }
    }

    /**
     * The available types of measurement for a catch
     */
    public enum MeasurementType {
        /**
         * METRIC measurement in kg
         */
        METRIC,
        /**
         * IMPERIAL measuremtn in oz
         */
        IMPERIAL
    }
}
