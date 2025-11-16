import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Stack;
public class Stopwatch implements ActionListener{
    // PriorityQueue that orders entries so the largest total time (HH:MM:SS) is at the head.
    PriorityQueue<String> savedAmountCalendar = new PriorityQueue<String>(new java.util.Comparator<String>() {
        public int compare(String a, String b) {
            long sa = parseSeconds(a);
            long sb = parseSeconds(b);
            // descending order: larger seconds should come first
            if (sa < sb) return 1;
            if (sa > sb) return -1;
            return 0;
        }
    }); //5 PRIORITY QUEUE DATA STRUCTURE IS USED TO SORT THE SAVED TIMES IN THE CALENDAR SO THAT THE HIGHEST TIME IS SHOWN FIRST
    Stack<String> contentStack = new Stack<>(); // -- 3.0 STACK A DATA STRUCTURE IS USED TO MAKE SURE THAT THE LATEST DATA SAVED BY THE USER IS SHOWED FIRST
    Database database = new Database();;
    //Time elapsed, Date this was recorded.
    DefaultListModel<String> model = new DefaultListModel<>();
    JList<String> Jlist = new JList<>(model);
    JFrame frame = new JFrame(); // what is a jframe?
    JButton startButton = new JButton("Start");
    JButton resetButton = new JButton("Reset");
    JButton saveButton = new JButton("Save");
    JLabel timeLabel = new JLabel();

    int elapsedTime = 0;
    int seconds = 0;
    int minutes = 0;
    int hours = 0;
    boolean started = false;
    String seconds_string = String.format("%02d", seconds);// para ang i output kay 01,02,03
    String minutes_string = String.format("%02d", minutes);
    String hours_string = String.format("%02d", hours);

    Stopwatch(){
            timeLabel.setText(hours_string + ":" + minutes_string + ":" + seconds_string);
            timeLabel.setBounds(100,100,200,100);//x,y,width,height sa size sa label(ang x and y kay position niya)
            timeLabel.setFont(new Font("Verdana", Font.PLAIN, 35));//font type, font style, font size
            timeLabel.setBorder(BorderFactory.createBevelBorder(1));//para naay border sa label\
            timeLabel.setOpaque(true);//para makita ang background color
            timeLabel.setHorizontalAlignment(JTextField.CENTER);//para ang text kay centered

            startButton.setBounds(50,200,100,50);//x,y,width,height sa size sa button
            startButton.setFont(new Font("Verdana", Font.PLAIN, 20));
            startButton.setFocusable(false);//para dili ma focus ang button(visual thing)
            startButton.addActionListener(this);//para ma trigger ang actionPerformed method kung i click ang button

            saveButton.setBounds(150,200,100,50);//x,y,width,height sa size sa button
            saveButton.setFont(new Font("Verdana", Font.PLAIN, 20));
            saveButton.setFocusable(false);//para dili ma focus ang button(visual thing)
            saveButton.addActionListener(this);


            resetButton.setBounds(250,200,100,50);//x,y,width,height sa size sa button
            resetButton.setFont(new Font("Verdana", Font.PLAIN, 20));
            resetButton.setFocusable(false);//para dili ma focus ang button(visual thing)
            resetButton.addActionListener(this);//para ma trigger ang actionPerformed method kung i click ang button

            //Jlist.setBounds(150, 400, 200, 100);
            Jlist.setBounds(100,275,200,250);


            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(null);
            mainPanel.setPreferredSize(new Dimension(430, 700)); // Height larger than scrollPane
            mainPanel.add(timeLabel);
            mainPanel.add(startButton);
            mainPanel.add(saveButton);
            mainPanel.add(resetButton);
            mainPanel.add(Jlist);

            JScrollPane scrollPane = new JScrollPane(mainPanel);
            scrollPane.setBounds(0, 0, 420, 420);

            frame.add(scrollPane);


            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);// para inig tuplok sa x kay mo close ang jframe
            frame.setSize(435,420);
            frame.setLayout(null);//idk what dis do
            frame.setVisible(true);//dapat at the end kay sometimes doesnt work
    }

    Timer timer = new Timer(1000, new ActionListener(){

        public void actionPerformed(ActionEvent e){
                elapsedTime = elapsedTime + 1000;
                hours = (elapsedTime/3600000); //there is 3600000 milliseconds in an hour
                minutes = (elapsedTime/60000) % 60; //there is 60000 milliseconds in a minute, and we use % 60 to get the remainder of minutes after hours
                seconds = (elapsedTime/1000) % 60; //there is 1000 milliseconds in a second, and we use % 60 to get the remainder of seconds after minutes
                //so basically iyaha gi buhat kay ang elapsed time mao ang base which is all in unfiltered seconds then gi convert niya into hours minutes and seconds

                seconds_string = String.format("%02d", seconds);
                minutes_string = String.format("%02d", minutes);
                hours_string = String.format("%02d", hours);
                timeLabel.setText(hours_string + ":" + minutes_string + ":" + seconds_string);
        }
    });

    @Override
    public void actionPerformed(ActionEvent e) {
            if(e.getSource()==startButton){
                if(started==false){
                        started = true;
                        startButton.setText("STOP");
                        start();
                }
                else {
                        started = false;
                        startButton.setText("START");
                        stop(); //stop the timer
                }
            }

            if(e.getSource()==resetButton){
                    started=false;
                    startButton.setText("START");
                    reset();
            }

            if(e.getSource()==saveButton){
                    // Here you can implement the save functionality, e.g., saving the elapsed time to a file or database
                    if(elapsedTime > 0){
                            save();
                            JOptionPane.showMessageDialog(frame, "Time saved: " + hours_string + ":" + minutes_string + ":" + seconds_string);
                            reset();
                            startButton.setText("START");
                    } else{
                            JOptionPane.showMessageDialog(frame, "Start the timer first");
                    }
            }
    }

    void start() {
            timer.start();
    }

    void stop(){
            timer.stop();
    }

    void reset(){
            timer.stop();
            elapsedTime=0;
            seconds = 0;
            minutes = 0;
            hours = 0;

            seconds_string = String.format("%02d", seconds);
            minutes_string = String.format("%02d", minutes);
            hours_string = String.format("%02d", hours);
            timeLabel.setText(hours_string + ":" + minutes_string + ":" + seconds_string);
    }

    void save(){

            database.addSavedTimeDaily(hours,minutes,seconds);
            model.clear();
            for(int i = 0; i < database.getTimeDaily().size(); i++){ //--2 This Traverses (Algorithm) the 1d array, the array is private in another class so the function getSavedTimeDailyContent accesses it for this class and sends out an index to get the specific data. A for loop is used to traverse the array
                contentStack.push(database.getSavedTimeDailyContent(i)); //--3.1 all data from the array is transferred to the stack
            }
            while (!contentStack.isEmpty()) {
                model.addElement(contentStack.pop()); //--3.2 all data inside the stack is popped (removed) and put inside the model to show it to the user.
            }

            for(int i = 0; i < database.getTimeDaily().size(); i++) {
                savedAmountCalendar.offer(database.getSavedTimeDailyContent(i));
            }

            while (!savedAmountCalendar.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                LocalDate today = LocalDate.now();
                // compute total hours for today first and put it at the top
                String totalHms = database.getTotalHoursDaily(today);
                sb.append("Total Hours: ").append(totalHms);

                // then append the saved entries under the total
                while (!savedAmountCalendar.isEmpty()) {
                    if (sb.length() > 0) sb.append("\n");
                    sb.append(savedAmountCalendar.poll());
                }

                Calendro.setTextForDate(today.getYear(), today.getMonthValue() - 1, today.getDayOfMonth(), sb.toString());
            }



        database.runSaveCalendar();
    }

    // parse "HH:MM:SS" (or returns 0 for malformed input)
    private static long parseSeconds(String s) {
        if (s == null) return 0L;
        s = s.trim();
        String token = s;
        if (s.contains(" ")) {
            String[] parts = s.split("\\s+");
            for (String p : parts) {
                if (p.matches("\\d{1,}:\\d{1,}:\\d{1,}")) { token = p; break; }
            }
        }
        String[] parts = token.split(":");
        if (parts.length != 3) return 0L;
        try {
            long h = Long.parseLong(parts[0]);
            long m = Long.parseLong(parts[1]);
            long sec = Long.parseLong(parts[2]);
            if (h < 0 || m < 0 || sec < 0) return 0L;
            return h * 3600L + m * 60L + sec;
        } catch (NumberFormatException ex) {
            return 0L;
        }
    }
}
