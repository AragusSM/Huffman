import java.io.IOException;
import java.util.ArrayList;

/*  Student information for assignment:
 *
 *  On My honor, Michael Ning , this programming assignment is MY own work
 *  and I have not provided this code to any other student.
 *
 *  Number of slip days used: 1
 *
 *  Student 1 (Student whose turnin account is being used)
 *  UTEID: zmn97
 *  email address: ningzy358@gmail.com
 *  Grader name: Henry Liu
 *
 */
//class that builds the HuffmanTree
public class HuffmanTree {

	private BitInputStream bits;
	private int[] freq;
	private HuffPriorityQueue queue;
	private TreeNode root;
	// stores the int values of bits in order of occurence in the file
	private ArrayList<Integer> bitValues;

	// constructor given a bitstream
	// pre: in cannot be null
	// post: creates a bit input stream and a frequency map
	public HuffmanTree(BitInputStream in) {
		if (in == null) {
			throw new IllegalArgumentException("bitinputstream cannot be null");
		}
		bits = in;
		freq = new int[IHuffConstants.ALPH_SIZE + 1];
		bitValues = new ArrayList<Integer>();
	}

	// constructor to build Huffman Tree from frequencies found in header, used in
	// decompressor.
	// pre: freq cannot be null
	public HuffmanTree(int[] freq) {
		if (freq == null) {
			throw new IllegalArgumentException("frequencies cannot be null");
		}
		this.freq = freq;
	}

	// default constructor
	public HuffmanTree() {

	}

	// given a bit input stream set to the header, reads the information and
	// recreates a tree, used for decompression
	// pre: in cannot be null
	// post: builds a tree with leaf nodes containing values, frequencu of 1, and
	// internal nodes containing no values and no frequencies. (null values are 0)
	public void recreateTree(BitInputStream in) throws IOException {
		if (in == null) {
			throw new IllegalArgumentException("input stream cannot be null.");
		}
		int size = in.readBits(IHuffConstants.BITS_PER_INT);
		TreeNode root;
		// we know the root will be an internal node because of psuedo_EOF, unless the file is empty in which case, the psuedo_EOF will be the root.
		if(size == 1) {
			in.readBits(1);
			int PEOF = in.readBits(IHuffConstants.BITS_PER_WORD + 1);
			this.root = new TreeNode(PEOF, 1);
		}else {
			//size will always be 1 or greater given psuedo_EOF value
			in.readBits(1);
			root = new TreeNode(0, 0);
			int[] currentSize = new int[] { 1 };
			addNodes(size, currentSize, root, in);
			this.root = root;
		}
	}

	// recursively create a tree following pre order traversal. if the bit read in
	// is a 1, set the node's value to the next 9 bits read. otherwise, create a new
	// node to the left and right and call this method once more.
	public void addNodes(int size, int[] currentSize, TreeNode current, BitInputStream in) throws IOException {
		// stop when current size is equal to the total size
		if (currentSize[0] < size) {
			int value = in.readBits(1);
			if (value == 0) {
				current.setLeft(new TreeNode(0, 0));
				currentSize[0]++;
				addNodes(size, currentSize, current.getLeft(), in);
			} else {
				// value is 1
				int bitValue = in.readBits(IHuffConstants.BITS_PER_WORD + 1);
				current.setLeft(new TreeNode(bitValue, 1));
			}
		}
		if (currentSize[0] < size) {
			// go down right tree
			int value2 = in.readBits(1);
			if (value2 == 0) {
				current.setRight(new TreeNode(0, 0));
				currentSize[0]++;
				addNodes(size, currentSize, current.getRight(), in);
			} else {
				int bitValue = in.readBits(IHuffConstants.BITS_PER_WORD + 1);
				current.setRight(new TreeNode(bitValue, 1));
			}
		}
	}

	// gets the frequency of each bit pattern and adds them to the frequency count
	private void getFreq() throws IOException {
		int inbits = bits.readBits(IHuffConstants.BITS_PER_WORD);
		while (inbits != -1) {
			freq[inbits]++;
			bitValues.add(inbits);
			inbits = bits.readBits(IHuffConstants.BITS_PER_WORD);
		}
		// set the psuedoEOF frequency to 1
		freq[IHuffConstants.ALPH_SIZE] = 1;
	}

	// returns the array of frequencies with each corresponding bit.
	// pre: freq cannot be null
	public int[] frequencies() {
		if (freq == null) {
			throw new IllegalStateException("The frequency map has not yet been created.");
		}
		return freq;
	}

	// creates the priority queue in order to construct this tree
	public HuffPriorityQueue createQueue() throws IOException {
		getFreq();
		queue = new HuffPriorityQueue(freq);
		return queue;
	}

	// recreates the priority queue for decompression
	public HuffPriorityQueue recreateQueue() {
		queue = new HuffPriorityQueue(freq);
		return queue;
	}

	// dequeues two nodes and enqueues the resulting node
	// pre: need at least two nodes left
	// post: combine two nodes, with the new node having a frequency of its children
	// combined and no value (technically -1);
	// we will just determine if it is a leaf node or not in order to separate.
	private TreeNode combine() {
		if (queue.size() < 2) {
			throw new IllegalStateException("size of the queue must be 2 or greater");
		}
		TreeNode left = queue.dequeue();
		TreeNode right = queue.dequeue();
		TreeNode newNode = new TreeNode(left, -1, right);
		queue.enqueue(newNode);
		return newNode;
	}

	// uses recursion to build a huffman tree.
	public void buildTree() {
		// base case, 1 element left in queue
		// recursive step, if size is 2 or greater, call combine and set the root equal
		// to the newly created node.
		if (queue.size() > 1) {
			root = combine();
			buildTree();
		}
	}

	// gets the bits as they come in order
	public ArrayList<Integer> getData() {
		return bitValues;
	}

	// print binary tree method, useful for debugging purposes
	public void printTree() {
		printTree(root, "");
	}

	// uses recursion to print the tree horizontally.
	private void printTree(TreeNode n, String spaces) {
		if (n != null) {
			printTree(n.getRight(), spaces + "  ");
			System.out.println(spaces + n.getValue() + " " + n.getFrequency());
			printTree(n.getLeft(), spaces + "  ");
		}
	}

	// generates the huffman keys for this tree
	public String[] getMap() {
		// size of alphabet plus one for the psuedoEOF
		String[] paths = new String[IHuffConstants.ALPH_SIZE + 1];
		getPaths(paths, "", root);
		return paths;
	}

	// recursive add method to add to the array index representing value and String
	// representing a path. Since Strings are immutable we do not need to undo each
	// step
	private void getPaths(String[] paths, String currentPath, TreeNode current) {
		// base case, we've reached a leaf node
		if (current.isLeaf()) {
			int value = current.getValue();
			paths[value] = currentPath;
		} else if (current.getLeft() == null) {
			// only right path open
			getPaths(paths, currentPath + 1, current.getRight());
		} else if (current.getRight() == null) {
			// only left path open
			getPaths(paths, currentPath + 0, current.getLeft());
		} else {
			// both paths open
			getPaths(paths, currentPath + 0, current.getLeft());
			getPaths(paths, currentPath + 1, current.getRight());
		}
	}

	// use recursion to count the number of node stored in this tree
	public int getNumNodes() {
		TreeNode current = root;
		return countNodes(current);
	}

	// method that does the main work
	private int countNodes(TreeNode current) {
		int count = 0;
		// base case is if the node is null, otherwise, add 1 to count and progress to
		// the left and right subtrees.
		if (current != null) {
			count += 1;
			count += countNodes(current.getLeft());
			count += countNodes(current.getRight());
		}
		return count;
	}

	// returns the root of this tree
	public TreeNode getRoot() {
		return root;
	}
}
