import java.io.*;
import java.util.*;
import java.util.Map;

/**
 * A program that uses Huffman encoding to compress and decompress files
 *
 * @author Evan Phillips, Dartmouth CS 10, Spring 2021
 * @author Georgia Dawahare, Dartmouth CS 10, Spring 2021
 */

public class Huffman extends TreeComparator {
    private Map<Character, Integer> characterCount; // Map with characters as keys and frequencies as values
    private TreeComparator frequencyCompare;        // compares the frequency counts in the root nodes of two trees
    private PriorityQueue<BinaryTree<Tree>> pq;     // set up to return tree with lowest frequency when asked to remove
    private Map<Character, String> huffmanMap;      // code map
    private BinaryTree<Tree> huffmanTree;           // Huffman code tree

    /**
     * Constructor, instantiates instance variables (except huffmanTree)
     */
    public Huffman() {
        characterCount = new TreeMap<Character, Integer>();
        frequencyCompare = new TreeComparator();
        pq = new PriorityQueue<BinaryTree<Tree>>(frequencyCompare);
        huffmanMap = new TreeMap<Character, String>();
    }

    /**
     * Creates a map (characterCount) with character and frequency pairs
     *
     * @param fileName name of file to be read from
     * @throws IOException handling file reading issues
     */
    public void frequencyTable(String fileName) throws IOException {
        BufferedReader input;

        // Open the file, if possible
        try {
            input = new BufferedReader(new FileReader(fileName));
        } catch (FileNotFoundException e) {
            System.err.println("Cannot open file.\n" + e.getMessage());
            return;
        }

        // Read the file
        try {
            int n = input.read();
            while (n != -1){  // while the file is not empty
                char character = (char) n;
                if (characterCount.containsKey(character)) {
                   int count = characterCount.get(character) + 1;
                    characterCount.put(character, count); // increment frequency if already in map
                } else {
                    characterCount.put(character, 1); // put in map if not already in map
                }
                n = input.read();   // increment n
            }
        } catch (IOException e) {
            System.err.println("IO error while reading.\n" + e.getMessage());
        }

        // Close the file, if possible
        try {
            input.close();
        } catch (IOException e) {
            System.err.println("Cannot close file.\n" + e.getMessage());
        }
    }

    /**
     * Puts initial trees in priority queue
     */
    public void createPriorityQueue() {
        for (Character character : characterCount.keySet()) {       // to iterate
            Tree newTree = new Tree(character, characterCount.get(character));  // Retrieve data for binary tree
            BinaryTree<Tree> newBinaryTree = new BinaryTree<Tree>(newTree);
            pq.add(newBinaryTree);      // Add new binary tree to priority queue
        }
    }

    /**
     * Creates Huffman code tree
     *
     * @return Fully constructed Huffman code tree
     */
    public BinaryTree<Tree> createHuffman() {
        if (pq.size() == 1){
            huffmanTree = pq.remove();
            return huffmanTree;
        }
        while (pq.size() > 1) {
            // Extract two lowest frequency trees from priority queue
            BinaryTree<Tree> t1 = pq.remove();
            BinaryTree<Tree> t2 = pq.remove();

            // Create new tree, attaching extracted trees as subtrees
            Tree r = new Tree();
            huffmanTree = new BinaryTree<Tree>(r, t1, t2);

            // Assign new tree a frequency that equals sum of frequencies of t1 and t2
            huffmanTree.data.frequency = t1.data.frequency + t2.data.frequency;

            pq.add(huffmanTree);
            }
        return huffmanTree;
    }

    /**
     * Creates A map that pairs characters with their code words
     *
     * @param constructedTree  Fully constructed Huffman code tree
     * @return Code map
     */
    public Map<Character, String> codeRetrieval(BinaryTree<Tree> constructedTree) {
        String code = "";
        if (constructedTree != null) {
            if (constructedTree.size() == 1) {      // Handles case of file with single character
                huffmanMap.put(constructedTree.getData().character, "0");
            }
            mapConstructor(constructedTree, code);
        }
        return huffmanMap;
    }

    /**
     * Helper function that traverses tree recursively and creates a code for each character in the Huffman code tree
     *
     * @param constructedTree  Fully constructed Huffman code tree
     * @param code String which will turn into a code for each character
     */
    public void mapConstructor(BinaryTree<Tree> constructedTree, String code) {
        if (constructedTree.hasLeft()) {
            huffmanMap.put(constructedTree.getLeft().getData().character, code + "0");
            mapConstructor(constructedTree.getLeft(), code + "0");
        }
        if (constructedTree.hasRight()) {
            huffmanMap.put(constructedTree.getRight().getData().character, code + "1");
            mapConstructor(constructedTree.getRight(), code + "1");
        }
    }

    /**
     * To compress a file
     *
     * @param fileName name of file to be compressed
     * @param huffMap Fully constructed Huffman code tree
     * @throws IOException handling file reading issues
     */
    public void compress(String fileName, Map<Character, String> huffMap)  throws IOException {
        BufferedReader compressInput;
        BufferedBitWriter bitOutput;

        // Open the file, if possible
        try {
            compressInput = new BufferedReader(new FileReader(fileName));
            bitOutput = new BufferedBitWriter(fileName + "_compressed");

        }
        catch(FileNotFoundException e) {
            System.err.println("Cannot open file. \n" + e.getMessage());
            return;
        }

        // Read the file
        try {
            boolean bit;
            int n = compressInput.read();
            while (n != -1){
                char character = (char) n;
                    String code = huffMap.get(character);

                    if (code != null) {         // Handles case if file empty
                        for (int i = 0; i < code.length(); i++) {
                            char c = code.charAt(i);
                            if (c == '0') {
                                bit = false;

                            } else {
                                bit = true;
                            }
                            bitOutput.writeBit(bit);
                        }
                    }
                n = compressInput.read();
            }
        }
        catch (IOException e) {
            System.err.println("IO error while reading.\n" + e.getMessage());
        }

        // Close the file, if possible
        try {
            compressInput.close();
            bitOutput.close();
        }
        catch (IOException e) {
            System.err.println("Cannot close file.\n" + e.getMessage());
        }
    }

    /**
     * To decompress a file
     *
     * @param fileName name of file to be decompressed
     * @param huffmanTree Fully constructed Huffman code tree
     * @throws IOException handling file reading issues
     */
    public void decompress(String fileName, BinaryTree<Tree> huffmanTree) throws IOException {

        BufferedBitReader bitInput;
        BufferedWriter output;

        // Open the file, if possible
        try {
            bitInput = new BufferedBitReader(fileName + "_compressed");
            output = new BufferedWriter(new FileWriter(fileName + "_decompressed"));
        } catch (FileNotFoundException e) {
            System.err.println("Cannot open file. \n" + e.getMessage());
            return;
        }

        // Read the file
        try {
            BinaryTree<Tree> current = huffmanTree;     // Keep track of path so far
            while (bitInput.hasNext()) {
                boolean bit = bitInput.readBit();
                if (bit && current.getRight() != null) { current = current.getRight(); }    // traverse right

                if (!bit && current.getLeft() != null) { current = current.getLeft(); }    // traverse left

                if (current.isLeaf()) {
                    char c = current.getData().getCharacter();
                    output.write(c);                // write character into output
                    current = huffmanTree;          // reset current
                }
            }
        }
        catch(IOException e){
                System.err.println("IO error while reading.\n" + e.getMessage());
            }

            // Close the file, if possible
            try {
                bitInput.close();
                output.close();
            } catch (IOException e) {
                System.err.println("Cannot close file.\n" + e.getMessage());
            }
        }

    public static void main(String[] args) throws Exception {
        // Test 1
        Huffman huffman1 = new Huffman();
        huffman1.frequencyTable("inputs/Hello");
        huffman1.createPriorityQueue();
        BinaryTree<Tree> huffTree1 = huffman1.createHuffman();
        Map<Character, String> huffMap1 = huffman1.codeRetrieval(huffTree1);
        huffman1.compress("inputs/Hello", huffMap1);
        huffman1.decompress("inputs/Hello", huffTree1);

        // Test 2
        Huffman huffman2 = new Huffman();
        huffman2.frequencyTable("inputs/USConstitution.txt");
        huffman2.createPriorityQueue();
        BinaryTree<Tree> huffTree2 = huffman2.createHuffman();
        Map<Character, String> huffMap2 = huffman2.codeRetrieval(huffTree2);
        huffman2.compress("inputs/USConstitution.txt", huffMap2);
        huffman2.decompress("inputs/USConstitution.txt", huffTree2);

        // Test 3
        Huffman huffman3 = new Huffman();
        huffman3.frequencyTable("inputs/WarAndPeace.txt");
        huffman3.createPriorityQueue();
        BinaryTree<Tree> huffTree3 = huffman3.createHuffman();
        Map<Character, String> huffMap3 = huffman3.codeRetrieval(huffTree3);
        huffman3.compress("inputs/WarAndPeace.txt", huffMap3);
        huffman3.decompress("inputs/WarAndPeace.txt", huffTree3);

        // Test 4
        Huffman huffman4 = new Huffman();
        huffman4.frequencyTable("inputs/J");
        huffman4.createPriorityQueue();
        BinaryTree<Tree> huffTree4 = huffman4.createHuffman();
        Map<Character, String> huffMap4 = huffman4.codeRetrieval(huffTree4);
        huffman4.compress("inputs/J", huffMap4);
        huffman4.decompress("inputs/J", huffTree4);

        // Test 5
        Huffman huffman5 = new Huffman();
        huffman5.frequencyTable("inputs/HHHHHHHHHHH");
        huffman5.createPriorityQueue();
        BinaryTree<Tree> huffTree5 = huffman1.createHuffman();
        Map<Character, String> huffMap5 = huffman1.codeRetrieval(huffTree1);
        huffman1.compress("inputs/HHHHHHHHHHH", huffMap5);
        huffman1.decompress("inputs/HHHHHHHHHHH", huffTree5);

        // Test 6
        Huffman huffman6 = new Huffman();
        huffman6.frequencyTable("inputs/Empty");
        huffman6.createPriorityQueue();
        BinaryTree<Tree> huffTree6 = huffman4.createHuffman();
        Map<Character, String> huffMap6 = huffman4.codeRetrieval(huffTree4);
        huffman6.compress("inputs/Empty", huffMap4);
        huffman6.decompress("inputs/Empty", huffTree4);

    }
}

