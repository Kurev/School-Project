import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.awt.Point;
import java.util.PriorityQueue;

public class Calendro extends JFrame {
    static JLabel lblMonth;
    static JButton btnPrev, btnNext;
    static JTable tblCalendar;
    static JComboBox<String> cmbYear;
    static JFrame frmMain;
    static Container pane;
    static DefaultTableModel mtblCalendar;
    static JScrollPane stblCalendar;
    static JPanel pnlCalendar;
    static int realYear, realMonth, realDay, currentYear, currentMonth;
    // store vertical scroll (pixel) offset for each rendered cell (row,col)
    static Map<Point, Integer> cellScrollY = new HashMap<>();
    // pixel amount to scroll per mouse-wheel notch
    static final int WHEEL_SCROLL_STEP = 20;

    Calendro() {
        // Frame setup - open maximized to occupy whole screen
        frmMain = new JFrame("Calendar");
        frmMain.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frmMain.setExtendedState(JFrame.MAXIMIZED_BOTH);
        pane = frmMain.getContentPane();
        pane.setLayout(new BorderLayout());

        // Create components
        lblMonth = new JLabel("January", SwingConstants.CENTER);
        cmbYear = new JComboBox<>();
        btnPrev = new JButton("<<");
        btnNext = new JButton(">>");
        mtblCalendar = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int rowIndex, int mColIndex) {
                return false;
            }
        };
        tblCalendar = new JTable(mtblCalendar);
        stblCalendar = new JScrollPane(tblCalendar);

        // Main panel with titled border
        pnlCalendar = new JPanel(new BorderLayout());
        pnlCalendar.setBorder(BorderFactory.createTitledBorder("Calendar"));

        // Top controls panel
        JPanel topControls = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 8));
        topControls.add(btnPrev);
        topControls.add(lblMonth);
        topControls.add(btnNext);
        topControls.add(cmbYear);

        pnlCalendar.add(topControls, BorderLayout.NORTH);
        pnlCalendar.add(stblCalendar, BorderLayout.CENTER);

        pane.add(pnlCalendar, BorderLayout.CENTER);

        // Table setup
        String[] headers = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String header : headers) mtblCalendar.addColumn(header);
        tblCalendar.getTableHeader().setResizingAllowed(false);
        tblCalendar.getTableHeader().setReorderingAllowed(false);

        mtblCalendar.setColumnCount(7);
        mtblCalendar.setRowCount(6);

        // Use a text area renderer so cells support wrapping multiline text
        tblCalendar.setDefaultRenderer(Object.class, new TextAreaRenderer());

        // Fill year combo box
        GregorianCalendar cal = new GregorianCalendar();
        realDay = cal.get(GregorianCalendar.DAY_OF_MONTH);
        realMonth = cal.get(GregorianCalendar.MONTH);
        realYear = cal.get(GregorianCalendar.YEAR);
        currentMonth = realMonth;
        currentYear = realYear;

        for (int i = realYear - 100; i <= realYear + 100; i++) {
            cmbYear.addItem(String.valueOf(i));
        }

        // Listeners
        btnPrev.addActionListener(new btnPrev_Action());
        btnNext.addActionListener(new btnNext_Action());
        cmbYear.addActionListener(new cmbYear_Action());

        // Adjust column widths and row heights when the viewport size changes
        stblCalendar.getViewport().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                resizeTableCells();
            }
        });

        // forward mouse wheel gestures to per-cell scroll offsets so each day can scroll
        tblCalendar.addMouseWheelListener(new MouseWheelListener() {
            public void mouseWheelMoved(MouseWheelEvent e) {
                Point p = e.getPoint();
                int row = tblCalendar.rowAtPoint(p);
                int col = tblCalendar.columnAtPoint(p);
                if (row < 0 || col < 0) return;

                Object val = mtblCalendar.getValueAt(row, col);
                String text = (val == null) ? "" : val.toString();

                // measure content height for the given column width
                int colWidth = tblCalendar.getColumnModel().getColumn(col).getWidth();
                JTextArea measure = new JTextArea();
                measure.setLineWrap(true);
                measure.setWrapStyleWord(true);
                measure.setFont(new Font("SansSerif", Font.PLAIN, 14));
                measure.setText(text);
                measure.setSize(colWidth, Short.MAX_VALUE);
                int contentHeight = measure.getPreferredSize().height;

                int rowHeight = tblCalendar.getRowHeight(row);
                int maxScroll = Math.max(0, contentHeight - rowHeight);

                Point key = new Point(row, col);
                int cur = 0;
                Integer v = cellScrollY.get(key);
                if (v != null) cur = v.intValue();

                // wheelRotation > 0 means scroll down (content moves up), so add
                int delta = e.getWheelRotation() * WHEEL_SCROLL_STEP;
                int next = cur + delta;
                if (next < 0) next = 0;
                if (next > maxScroll) next = maxScroll;

                if (next != cur) {
                    cellScrollY.put(key, next);
                    tblCalendar.repaint();
                    e.consume();
                }
            }
        });

        // Display current calendar
        refreshCalendar(realMonth, realYear);

        frmMain.setVisible(true);
    }

    // Set row height large enough (or proportional) so cells can hold multiline text
    private void resizeTableCells() {
        Dimension viewSize = stblCalendar.getViewport().getSize();
        int height = viewSize.height;
        int width = viewSize.width;
        if (height <= 0 || width <= 0) return;

        int rowHeight = Math.max(80, height / 6); // ensure cells are large enough
        tblCalendar.setRowHeight(rowHeight);

        // set columns to equal widths
        TableColumnModel colModel = tblCalendar.getColumnModel();
        int colWidth = width / colModel.getColumnCount();
        for (int i = 0; i < colModel.getColumnCount(); i++) {
            colModel.getColumn(i).setPreferredWidth(colWidth);
        }
    }

    public static void refreshCalendar(int month, int year) {
        String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};

        lblMonth.setText(months[month]);
        cmbYear.setSelectedItem(String.valueOf(year));

        // Clear table
        for (int i = 0; i < 6; i++)
            for (int j = 0; j < 7; j++)
                mtblCalendar.setValueAt(null, i, j);

        // Compute first day and number of days
        GregorianCalendar cal = new GregorianCalendar(year, month, 1);
        int nod = cal.getActualMaximum(GregorianCalendar.DAY_OF_MONTH);
        int som = cal.get(GregorianCalendar.DAY_OF_WEEK);

        // Fill calendar (put day numbers as strings; you can later store longer text)
        for (int i = 1; i <= nod; i++) {
            int row = (i + som - 2) / 7;
            int column = (i + som - 2) % 7;
            mtblCalendar.setValueAt(String.valueOf(i), row, column);
        }

        // Ensure renderer highlights current day and table sizing will be updated
        tblCalendar.setDefaultRenderer(Object.class, new TextAreaRenderer());

        // Trigger a resize to set initial row heights and column widths
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (frmMain != null) {
                    Component viewport = stblCalendar.getViewport();
                    if (viewport != null) viewport.revalidate();
                }
            }
        });
    }

    // Renderer using a JTextArea inside a JScrollPane so each day becomes scrollable
    static class TextAreaRenderer implements TableCellRenderer {
        private final JTextArea ta;
        private final JScrollPane scroll;

        public TextAreaRenderer() {
            ta = new JTextArea();
            ta.setLineWrap(true);
            ta.setWrapStyleWord(true);
            ta.setOpaque(true);
            ta.setBorder(null);
            ta.setFont(new Font("SansSerif", Font.PLAIN, 14));
            ta.setEditable(false);
            ta.setMargin(new Insets(6, 6, 6, 6));

            scroll = new JScrollPane(ta);
            scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
            scroll.setBorder(null);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            String text = (value == null) ? "" : value.toString();
            ta.setText(text);
            ta.setForeground(Color.BLACK);
            ta.setBackground(Color.WHITE);

            // highlight today's day number background (keeps the same row height - scrolling handles overflow)
            if (!text.isEmpty()) {
                try {
                    int dayVal = Integer.parseInt(text.trim().split("\\s+")[0]);
                    if (dayVal == realDay && currentMonth == realMonth && currentYear == realYear) {
                        ta.setBackground(new Color(220, 220, 255));
                    }
                } catch (NumberFormatException ignored) {
                }
            }

            // set the viewport size to match the current cell so large content will be scrollable
            int colWidth = table.getColumnModel().getColumn(column).getWidth();
            int rowHeight = table.getRowHeight(row);
            scroll.setPreferredSize(new Dimension(colWidth, rowHeight));

            // apply per-cell stored vertical scroll offset (pixels)
            Point key = new Point(row, column);
            int y = 0;
            Integer stored = cellScrollY.get(key);
            if (stored != null) y = stored.intValue();

            // ensure y within content bounds
            ta.setSize(colWidth, Short.MAX_VALUE);
            int contentH = ta.getPreferredSize().height;
            int maxY = Math.max(0, contentH - rowHeight);
            if (y < 0) y = 0;
            if (y > maxY) y = maxY;
            scroll.getViewport().setViewPosition(new Point(0, y));

            // ensure the view starts at the top for each cell render
            ta.setCaretPosition(0);

            return scroll;
        }
    }

    static class btnPrev_Action implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (currentMonth == 0) {
                currentMonth = 11;
                currentYear -= 1;
            } else {
                currentMonth -= 1;
            }
            refreshCalendar(currentMonth, currentYear);
        }
    }

    static class btnNext_Action implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (currentMonth == 11) {
                currentMonth = 0;
                currentYear += 1;
            } else {
                currentMonth += 1;
            }
            refreshCalendar(currentMonth, currentYear);
        }
    }

    static class cmbYear_Action implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (cmbYear.getSelectedItem() != null) {
                currentYear = Integer.parseInt(cmbYear.getSelectedItem().toString());
                refreshCalendar(currentMonth, currentYear);
            }
        }
    }

    // java
// Add these methods inside class Calendro (in `src/Calendro.java`)


    // Find the table cell (row,col) that contains the given day number.
// Returns null if not found.
    private static Point findCellForDay(int day) { // 4 BINARY SEARCH an algorithm which This method used to locate the table cell for a given day by first building a GregorianCalendar for currentYear/currentMonth and computing the flattened start and end indices for the month's days (e.g. int startIndex = som - 1; and int endIndex = startIndex + nod - 1;), then it validates the day (if (day < 1 || day > nod) return null;) and performs a binary search over those flattened indices: each midpoint is converted to a day with int midDay = (mid - startIndex) + 1;, and when it matches the requested day the code maps the flattened index back to table coordinates with int row = mid / cols; int col = mid % cols; and returns a Point(row, col). The approach yields O(log n) lookup by exploiting the contiguous day range in the flattened grid, though the same result can be computed directly in O(1) with simple arithmetic if preferred.
        GregorianCalendar cal = new GregorianCalendar(currentYear, currentMonth, 1);
        int som = cal.get(GregorianCalendar.DAY_OF_WEEK);
        int nod = cal.getActualMaximum(GregorianCalendar.DAY_OF_MONTH);
        int startIndex = som - 1; // flattened index of day 1
        int endIndex = startIndex + nod - 1;

        if (day < 1 || day > nod) return null;

        int cols = mtblCalendar.getColumnCount();
        int lo = startIndex, hi = endIndex;
        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            int midDay = (mid - startIndex) + 1;
            if (midDay == day) {
                int row = mid / cols;
                int col = mid % cols;
                return new Point(row, col);
            } else if (midDay < day) {
                lo = mid + 1;
            } else {
                hi = mid - 1;
            }
        }
        return null;
    }

    // Put plain text into a specific date cell.
// - year: full year (e.g. 2025)
// - month: 0-based month (0 = January)
// - day: day of month (1..31)
// Returns true if the cell was found and updated.
    public static boolean setTextForDate(int year, int month, int day, String text) {
        // ensure requested month/year is visible
        if (currentYear != year || currentMonth != month) {
            currentYear = year;
            currentMonth = month;
            refreshCalendar(month, year);
        }

        Point p = findCellForDay(day);
        if (p == null) return false;

        int row = p.x, col = p.y;
        Object existing = mtblCalendar.getValueAt(row, col);
        String base = String.valueOf(day);
        String newValue;

        if (existing == null || existing.toString().trim().isEmpty()) {
            newValue = base + "\n" + text;
        } else {
            String s = existing.toString();
            // replace any existing note and keep the day number on first line
            String[] parts = s.split("\\n", 2);
            newValue = parts[0].trim().equals(base) ? (base + "\n" + text) : (base + "\n" + text);
        }

        mtblCalendar.setValueAt(newValue, row, col);

        // make cell visible and refresh renderer/layout
        Rectangle rect = tblCalendar.getCellRect(row, col, true);
        tblCalendar.scrollRectToVisible(rect);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (stblCalendar != null && stblCalendar.getViewport() != null) {
                    stblCalendar.getViewport().revalidate();
                }
                tblCalendar.repaint();
            }
        });

        return true;
    }

    // Read the note stored for a date (returns text after the day number, or empty string)
    public static String getTextForDate(int year, int month, int day) {
        if (currentYear != year || currentMonth != month) {
            currentYear = year;
            currentMonth = month;
            refreshCalendar(month, year);
        }
        Point p = findCellForDay(day);
        if (p == null) return null;
        Object val = mtblCalendar.getValueAt(p.x, p.y);
        if (val == null) return "";
        String[] parts = val.toString().split("\\n", 2);
        return (parts.length > 1) ? parts[1] : "";
    }
}

/*
Usage example:
    // put "Meeting at 10am" into 15 March 2025
    Calendro.setTextForDate(2025, 2, 15, "Meeting at 10am");

    // read the note for that date
    String note = Calendro.getTextForDate(2025, 2, 15);
*/
