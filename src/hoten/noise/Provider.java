package hoten.noise;

import hoten.noise.strategy.PerlinNoise;
import hoten.noise.strategy.RadialNoise;
import java.util.HashMap;
import java.util.Map;

/**
 * This class provides noise strategies.
 * @author lbroquet
 */
public abstract class Provider {

    private static final Map<String, Noise> strategies = new HashMap<>();
    static {
        strategies.put("radial", new RadialNoise());
        strategies.put("perlin", new PerlinNoise());
    }

    public static Noise getNoise(String strategy) {
        return strategies.get(strategy);
    }

    private Provider() {
        // Singleton
    }
}
