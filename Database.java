import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class Database {

    private ArrayList<String> savedTimeDaily = new ArrayList<String>(); //1 ARRAY a data structure that is used to store data by the user
    private HashMap<LocalDate, ArrayList> saveCalendar = new HashMap<>();
    LocalDate currentDate = LocalDate.now();


    public void addSavedTimeDaily(int hours, int minutes, int seconds){
            savedTimeDaily.add(String.format("%02d:%02d:%02d", hours, minutes, seconds));
    }

    public void runSaveCalendar(){
            saveCalendar.put(currentDate, savedTimeDaily);
            System.out.println(saveCalendar.get(currentDate));
    }

    public String getSavedTimeDailyContent(int i){
        return savedTimeDaily.get(i);
    }


    public ArrayList<String> getTimeDaily(){
            return savedTimeDaily;
    }

    // Sum all "HH:MM:SS" entries for the specified date and return formatted total "HH:MM:SS".
    // If there are no entries for the date, returns "00:00:00".
    public String getTotalHoursDaily(java.time.LocalDate date) {
        ArrayList list = saveCalendar.get(date);
        // if no entry in saveCalendar, but date equals currentDate, use in-memory list
        if (list == null) {
            if (date.equals(currentDate)) {
                list = savedTimeDaily;
            } else {
                return "00:00:00";
            }
        }

        long totalSeconds = 0L;
        for (Object o : list) {
            if (o == null) continue;
            String s = o.toString().trim();
            String[] parts = s.split(":");
            if (parts.length == 3) {
                try {
                    int h = Integer.parseInt(parts[0]);
                    int m = Integer.parseInt(parts[1]);
                    int sec = Integer.parseInt(parts[2]);
                    totalSeconds += (long) h * 3600L + (long) m * 60L + sec;
                } catch (NumberFormatException ex) {
                    // ignore malformed entries
                }
            }
        }

        long hrs = totalSeconds / 3600L;
        long mins = (totalSeconds % 3600L) / 60L;
        long secs = totalSeconds % 60L;
        return String.format("%02d:%02d:%02d", hrs, mins, secs);
    }
}
