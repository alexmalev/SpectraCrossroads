package game;

/**
 * holds the traffic light state of the spectra system.
 *
 */
public class SystemState {
    private Color verticalLight;
    private Color horizontalLight;

    public SystemState(Color verticalLight, Color horizontalLight) {
        this.verticalLight = verticalLight;
        this.horizontalLight = horizontalLight;
    }
    public Color getVerticalLight() {
        return verticalLight;
    }


    public Color getHorizontalLight() {
        return horizontalLight;
    }


}
