package me.samsuik.sakura.explosion;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Sort explosion rays for better cache utilisation.
 * <pre>
 *   x +   Vanilla     Sorted
 * z @ z      8           5
 * - x      6   7       6   4
 *        4   @   5   7   @   3
 *          2   3       8   2
 *            1           1
 * </pre>
 */
@NullMarked
public final class SortExplosionRays {
    private static final Comparator<double[]> EXPLOSION_RAY_COMPARATOR = Comparator.comparingDouble(vec -> {
        final double sign = Math.signum(vec[0]);
        final double dir = (sign - 1) / 2;
        return sign + 8 + vec[2] * dir;
    });

    public static double[] sortExplosionRays(final DoubleArrayList rayCoords) {
        final List<double[]> explosionRays = new ArrayList<>();
        for (int index = 0; index < rayCoords.size(); index += 3) {
            final double[] vector = new double[3];
            rayCoords.getElements(index, vector, 0, 3);
            explosionRays.add(vector);
        }

        rayCoords.clear();
        explosionRays.sort(EXPLOSION_RAY_COMPARATOR);

        final double[] rays = new double[explosionRays.size() * 3];
        for (int i = 0; i < explosionRays.size() * 3; i++) {
            rays[i] = explosionRays.get(i / 3)[i % 3];
        }
        return rays;
    }
}
