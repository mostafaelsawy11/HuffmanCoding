import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.Comparator;

class Node implements Comparable<Node> {
    public char ch;
    public Node left;
    public Node right;
    public int freq;

    public Node(char ch, int freq) {
        this.ch = ch;
        this.freq = freq;
        left = right = null;
    }

    @Override
    public int compareTo(Node node) {
        return Integer.compare(node.freq, freq);
    }
}

class Encode {
    public static HashMap<Character, String> HuffmanCode(String input) {

        HashMap<Character, Integer> freqChar = new HashMap<>();
        for (char c : input.toCharArray()) {
            freqChar.put(c, freqChar.getOrDefault(c, 0) + 1);
        }
        PriorityQueue<Node> que = new PriorityQueue<>(Comparator.reverseOrder());
        for (char key : freqChar.keySet()) {
            que.add(new Node(key, freqChar.get(key)));
        }
        while (que.size() > 1) {
            Node left = que.poll();
            Node right = que.poll();
            Node merge = new Node('$', left.freq + right.freq);
            merge.left = left;
            merge.right = right;
            que.add(merge);
        }
        HashMap<Character, String> huffCoding = new HashMap<>();
        generalTree(que.peek(), "", huffCoding);
        return huffCoding;

    }

    private static void generalTree(Node root, String code, HashMap<Character, String> huffCoding) {
        if (root == null)
            return;
        if (root.ch != '$') {
            huffCoding.put(root.ch, code);
        }
        generalTree(root.left, code + "1", huffCoding);
        generalTree(root.right, code + "0", huffCoding);
    }

}

 class HuffmanGUI {
    private JFrame frame;
    private JButton chooseFileButton, compressButton, decompressButton;
    private JTextArea resultArea;
    private File selectedFile;

    public HuffmanGUI() {
        frame = new JFrame("Huffman Coding Algorithm");
        frame.setSize(400, 200);
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout());

        JLabel welcomeLabel = new JLabel("Welcome to Huffman Coding Algorithm");
        topPanel.add(welcomeLabel);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new FlowLayout());

        chooseFileButton = new JButton("Choose File");
        compressButton = new JButton("Compress");
        decompressButton = new JButton("Decompress");

        centerPanel.add(chooseFileButton);
        centerPanel.add(compressButton);
        centerPanel.add(decompressButton);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());

        resultArea = new JTextArea();
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);
        bottomPanel.add(scrollPane, BorderLayout.CENTER);

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(centerPanel, BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        chooseFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir"));
                int result = fileChooser.showOpenDialog(frame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    selectedFile = fileChooser.getSelectedFile();
                    resultArea.setText("Selected File: " + selectedFile.getName());
                }
            }
        });

        compressButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedFile != null) {
                    compressOption(selectedFile.getAbsolutePath());
                } else {
                    resultArea.setText("Please choose a file first.");
                }
            }
        });

        decompressButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedFile != null) {
                    decompressOption(selectedFile.getAbsolutePath());
                } else {
                    resultArea.setText("Please choose a file first.");
                }
            }
        });
    }

    public void display() {
        frame.setVisible(true);
    }

    private static void decompress(String compressed, HashMap<Character, String> huffcode) {
        HashMap<String, Character> decompress = new HashMap<>();
        for (char key : huffcode.keySet()) {
            decompress.put(huffcode.get(key), key);
        }
        String binarySubstring = compressed.substring(0, 8);
        int decimalValue = Integer.parseInt(binarySubstring, 2);
        String result = compressed.substring(8, compressed.length() - decimalValue);
        compressed = result;
        String str = "";
        String output = "";
        for (char c : compressed.toCharArray()) {
            str += c;
            if (decompress.containsKey(str)) {
                output += decompress.get(str);
                str = "";
            }
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("decompressed.txt"))) {
            // Write the content to the file
            writer.write(output);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadCompressed(String file) {

        HashMap<Character, String> overhead = new HashMap<>();
        String decompress = "";
        boolean f = true, z = true;
        char c = 0;
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            int currentByte;

            while ((currentByte = fileInputStream.read()) != -1) {
                if (currentByte == '\n') {
                    z = false;
                } else {
                    String binaryRepresentation = String.format("%8s", Integer.toBinaryString(currentByte & 0xFF))
                            .replace(' ', '0');
                    if (f & z) {
                        int decimalValue = Integer.parseInt(binaryRepresentation, 2);
                        c = (char) decimalValue;
                        f = false;
                    } else if (!f & z) {
                        String binarySubstring = binaryRepresentation.substring(0, 3);
                        int decimalValue = Integer.parseInt(binarySubstring, 2);
                        String result = binaryRepresentation.substring(3, 3 + decimalValue);
                        f = true;
                        overhead.put(c, result);
                    } else {
                        decompress += binaryRepresentation;
                    }

                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        decompress(decompress, overhead);
    }

    public static String compress(String filename) {
        StringBuilder line = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String inline;
            while ((inline = reader.readLine()) != null) {
                line.append(inline);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return line.toString();
    }

    private static void compressOption(String filename) {
        String data = compress(filename);
        Encode c = new Encode();
        HashMap<Character, String> h = c.HuffmanCode(data);
        String s = "";
        for (int i = 0; i < data.length(); i++) {
            s += h.get(data.charAt(i));
        }
        String output_file = "output.bin";
        try (OutputStream outputStream = new FileOutputStream(output_file)) {
            for (Character key : h.keySet()) {
                String binary_save = Integer.toBinaryString((int) key);
                String padding = String.format("%8s", binary_save).replace(' ', '0');
                int size = h.get(key).length();
                String str_size = Integer.toBinaryString(size);
                padding += String.format("%3s", str_size).replace(' ', '0');
                String value = h.get(key);
                padding += value;
                for (int i = padding.length(); i < 16; i++) {
                    padding += "0";
                }
                int decimal1 = Integer.parseInt(padding.substring(0, 8), 2);
                int decimal2 = Integer.parseInt(padding.substring(8), 2);
                byte byte1 = (byte) decimal1;
                byte byte2 = (byte) decimal2;

                outputStream.write(byte1);
                outputStream.write(byte2);

            }
            outputStream.write('\n');
            int numOfChar = 0;
            if (s.length() % 8 != 0) {
                int correct_size = ((s.length() / 8) + 1) * 8;
                for (int i = s.length(); i < correct_size; i++) {
                    s += "0";
                    numOfChar++;
                }
            }
            byte byte0 = (byte) numOfChar;
            outputStream.write(byte0);
            String empty = "";
            for (int i = 0; i < s.length(); i++) {
                if (empty.length() == 8) {
                    int number = Integer.parseInt(empty.substring(0, 8), 2);
                    byte byte1 = (byte) number;
                    outputStream.write(byte1);
                    empty = String.valueOf(s.charAt(i));
                } else {
                    empty += s.charAt(i);
                }
            }
            int number = Integer.parseInt(empty.substring(0, 8), 2);
            byte byte1 = (byte) number;
            outputStream.write(byte1);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void decompressOption(String filename) {
        loadCompressed(filename);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                HuffmanGUI huffmanGUI = new HuffmanGUI();
                huffmanGUI.display();
            }
        });
    }
}
