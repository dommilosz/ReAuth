package technicianlp.reauth;

public class timeMeasureUtil {
    public long start;
    public long getMeasure(){
        return System.currentTimeMillis()-start;
    }
    public String getMeasureFormated(){
        return getMeasure()+"ms";
    }
    timeMeasureUtil(){
        start = System.currentTimeMillis();
    }
}
