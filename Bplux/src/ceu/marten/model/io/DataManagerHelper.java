package ceu.marten.model.io;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ericchu on 2015-06-19.
 */
public class DataManagerHelper {

    static DataManagerHelper instance = null;

    String name = "";

    private DataManagerHelper() {
    }

    private void reset() {
        name = "";
    }

    public static DataManagerHelper getInstance() {
        if (instance == null) {
            instance = new DataManagerHelper();
        }
        return instance;
    }

    public String getName(String recordingName) {
        if (name.isEmpty()) {
            generateName(recordingName);
            return name;
        } else {
            String resultName = name;
            reset();
            return resultName;
        }
    }

    private void generateName(String recordingName) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd.HH.mm.ss");
        String formattedDate = sdf.format(new Date());
        name = recordingName + formattedDate;
    }
}