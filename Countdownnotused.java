import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import static java.lang.Character.isDigit;

public class Countdownnotused implements ActionListener{
    JFrame frame = new JFrame();
    JButton startButton = new JButton("START");
    JButton resetButton = new JButton("RESET");
    JLabel timeLabel = new JLabel();
    JLabel colon1 = new JLabel(":");
    JLabel colon2 = new JLabel(":");

    JPanel timeFieldContainer = new JPanel();
    JTextField hoursField = new JTextField("00",2);
    JTextField minutesField = new JTextField("00",2);
    JTextField secondsField = new JTextField("00",2);

    int hoursField_int;
    int minutesField_int;
    int secondsField_int;


    private int remainingTime = 0;//I think pwede na nako i .getText nlng diria instead of setter and getter
    int seconds = 0;
    int minutes = 0;
    int hours = 0;
    boolean started = false;

    String seconds_String = String.format("%02d", seconds);
    String minutes_String = String.format("%02d", minutes);
    String hours_String = String.format("%02d", hours);

    Timer timer = new Timer(1000, new ActionListener(){
        public void actionPerformed(ActionEvent e){
            if(remainingTime > 0){
                remainingTime = remainingTime - 1000;
                hours = remainingTime / 3600000;
                minutes = (remainingTime / 60000) % 60;
                seconds = (remainingTime / 1000) % 60;

                seconds_String = String.format("%02d", seconds);
                minutes_String = String.format("%02d", minutes);
                hours_String = String.format("%02d", hours);

                hoursField.setText(hours_String);
                minutesField.setText(minutes_String);
                secondsField.setText(seconds_String);
            } else {
                stop();
            }


        }
    });


    Countdownnotused(){

        timeFieldContainer.setBounds(100, 100, 200, 100);
        timeFieldContainer.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        timeFieldContainer.setOpaque(false);

        // Inside the Countdown constructor, after configuring timeFieldContainer:
        timeFieldContainer.setLayout(new BoxLayout(timeFieldContainer, BoxLayout.X_AXIS));


        hoursField.setFont(new Font("Verdana", Font.PLAIN, 35));
        hoursField.setBorder(BorderFactory.createEmptyBorder());
        hoursField.setBackground(frame.getBackground());
        minutesField.setFont(new Font("Verdana", Font.PLAIN, 35));
        minutesField.setBorder(BorderFactory.createEmptyBorder());
        minutesField.setBackground(frame.getBackground());
        secondsField.setFont(new Font("Verdana", Font.PLAIN, 35));
        secondsField.setBorder(BorderFactory.createEmptyBorder());
        secondsField.setBackground(frame.getBackground());
        colon1.setFont(new Font("Verdana", Font.PLAIN, 35));
        colon1.setBackground(Color.WHITE);
        colon2.setFont(new Font("Verdana", Font.PLAIN, 35));
        colon2.setBackground(Color.WHITE);

        hoursField.setHorizontalAlignment(JTextField.CENTER);
        minutesField.setHorizontalAlignment(JTextField.CENTER);
        secondsField.setHorizontalAlignment(JTextField.CENTER);

        timeFieldContainer.add(hoursField);
        timeFieldContainer.add(colon1);
        timeFieldContainer.add(minutesField);
        timeFieldContainer.add(colon2);
        timeFieldContainer.add(secondsField);

        startButton.setBounds(100,200,100,50);//x,y,width,height sa size sa button
        startButton.setFont(new Font("Verdana", Font.PLAIN, 20));
        startButton.setFocusable(false);//para dili ma focus ang button(visual thing)
        startButton.addActionListener(this);//para ma trigger ang actionPerformed method kung i click ang button

        resetButton.setBounds(200,200,100,50);//x,y,width,height sa size sa button
        resetButton.setFont(new Font("Verdana", Font.PLAIN, 20));
        resetButton.setFocusable(false);//para dili ma focus ang button(visual thing)
        resetButton.addActionListener(this);//para ma trigger ang actionPerformed method kung i click ang button

        frame.add(timeLabel);
        frame.add(startButton);
        frame.add(resetButton);
        frame.add(timeFieldContainer);

        frame.setSize(420,420);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(null);
        frame.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e){
        if(e.getSource() == startButton){
            if (started == false){
                start();
            } else{
                stop();
            }
        }

        if(e.getSource() == resetButton){
            reset();
        }
    }

    void start(){
        started = true;
        startButton.setText("STOP");
        hoursField_int = Integer.parseInt(hoursField.getText()) * 3600000;
        minutesField_int = Integer.parseInt(minutesField.getText()) * 60000;
        secondsField_int = Integer.parseInt(secondsField.getText()) * 1000;
        remainingTime = hoursField_int + minutesField_int + secondsField_int;
        timer.start();
    }

    void stop(){
        started = false;
        startButton.setText("START");
        timer.stop();
    }

    void reset(){
        started=false;
        startButton.setText("START");
        timer.stop();
        remainingTime=0;
        seconds = 0;
        minutes = 0;
        hours = 0;

        seconds_String = String.format("%02d", seconds);
        minutes_String = String.format("%02d", minutes);
        hours_String = String.format("%02d", hours);
        hoursField.setText(hours_String);
        minutesField.setText(minutes_String);
        secondsField.setText(seconds_String);
    }
}
