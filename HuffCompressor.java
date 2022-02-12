import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
//class for compressing a given file to hf. We will read each bit in the file, 
//use the given tree and its keys to convert the bits to Huffman encoding values, and write out
//to file along with magic number, STORE_COUNTS/STORE_TREE Constants, header, and psuedo_eof.
public class HuffCompressor {

	private BitInputStream in;
	private BitOutputStream out;
	private HuffmanTree tree;
	private String[] map;
	private int headerFormat;

	// constructor that constructs a huffman tree based on an input stream and sets
	// the input stream and output stream to the beginning of the file.
	// in and out cannot be null
	public HuffCompressor(InputStream in, OutputStream out) {
		if (in == null || out == null) {
			throw new IllegalArgumentException("streams cannot be null");
		}
		this.in = new BitInputStream(in);
		this.out = new BitOutputStream(out);
	}

	// alternate constructor for precompress method, in cannot be null
	public HuffCompressor(InputStream in) {
		if (in == null) {
			throw new IllegalArgumentException("input stream cannot be null");
		}
		this.in = new BitInputStream(in);
	}

	// set output stream incase this object was constructed using only input stream
	public void setBitOutputStream(OutputStream out) {
		this.out = new BitOutputStream(out);
	}

	// precompress method that creates a tree and gets the encoding map
	// returns the number of 8 bit ints * int per word value to get the number of
	// bits in original file.
	public int preCompress(int headerFormat) throws IOException {
		tree = new HuffmanTree(this.in);
		tree.createQueue();
		tree.buildTree();
		map = tree.getMap();
		this.headerFormat = headerFormat;
		int originalBits = tree.getData().size() * IHuffConstants.BITS_PER_WORD;
		return originalBits - calculateBits();
	}

	// calculate total number of bits written excluding padding. for the tree store
	// method, we have an option to not write and only count number of bits
	private int calculateBits() {
		final int MAGIC_NUMBER_BITS = IHuffConstants.BITS_PER_INT;
		final int FORMAT_BITS = IHuffConstants.BITS_PER_INT;
		int headerBits = IHuffConstants.BITS_PER_INT * IHuffConstants.ALPH_SIZE;
		// if the given format from preprocessCompress is tree, then count tree,
		// otherwise use store counts as default
		if (headerFormat == IHuffConstants.STORE_TREE) {
			headerBits = writeHeaderTree(false);
		}
		int bitsWritten = 0;
		// add the number of bits in the header
		bitsWritten += (MAGIC_NUMBER_BITS + FORMAT_BITS + headerBits);
		// adds the tree value of every bit that occurs in the file
		ArrayList<Integer> bitValues = tree.getData();
		for (int value : bitValues) {
			String bitSequence = map[value];
			bitsWritten += bitSequence.length();
		}
		// adds the PEOF length
		String PEOF = map[IHuffConstants.ALPH_SIZE];
		bitsWritten += PEOF.length();
		return bitsWritten;
	}

	// combines all the information int one compression and returns an int of how
	// many bits were written
	// output stream cannot be null;
	public int compress() throws IOException {
		if (out == null) {
			throw new IllegalStateException("Output stream cannot be null");
		}
		writeMagicNumber();
		writeFormat();
		if (headerFormat == IHuffConstants.STORE_TREE) {
			writeHeaderTree(true);
		}else {
			writeHeaderCounts();
		}
		compressBits();
		writePEOF();
		out.close();
		return calculateBits();
	}

	// compresses the actual data following the data in the compressed file
	private void compressBits() throws IOException {
		ArrayList<Integer> bitValues = tree.getData();
		for (int value : bitValues) {
			String bitSequence = map[value];
			for (int i = 0; i < bitSequence.length(); i++) {
				out.writeBits(1, bitSequence.charAt(i));
			}
		}
	}

	// writes to the file the identifier number that this is a huffman compressed
	// file
	private void writeMagicNumber() {
		out.writeBits(IHuffConstants.BITS_PER_INT, IHuffConstants.MAGIC_NUMBER);
	}

	// write out the format of the encoding.
	private void writeFormat() {
		out.writeBits(IHuffConstants.BITS_PER_INT, headerFormat);
	}

	// write out the header to recreate the tree when decompressing.
	private void writeHeaderCounts() {
		int[] freq = tree.frequencies();
		for (int i = 0; i < IHuffConstants.ALPH_SIZE; i++) {
			out.writeBits(IHuffConstants.BITS_PER_INT, freq[i]);
		}
	}

	// write out the header in standard tree format if passed true, otherwise only
	// count.
	// returns the number of bits written
	private int writeHeaderTree(boolean write) {
		int numBits = 0;
		int sizeOfTree = tree.getNumNodes();
		numBits += IHuffConstants.BITS_PER_INT;
		if (write) {
			out.writeBits(IHuffConstants.BITS_PER_INT, sizeOfTree);
		}

		TreeNode current = tree.getRoot();
		numBits += writeTree(current, write);
		return numBits;
	}

	// write out the tree header. 0 represents internal nodes and 1 represent leaf
	// node. the path is a preordertraversal through the tree. Everytime it lands on
	// a leaf print 9 bits representing the bit value of that leaf
	private int writeTree(TreeNode current, boolean write) {
		int count = 0;
		// base case is leaf node
		if (current.isLeaf()) {
			if (write) {
				// write one bit with value of one
				out.writeBits(1, 1);
				// write the bit representation of the value of the tree
				out.writeBits(IHuffConstants.BITS_PER_WORD + 1, current.getValue());
			}
			count += (1 + IHuffConstants.BITS_PER_WORD + 1);
		} else {
			if (write) {
				// not a leaf node, write a 0 and go to left and right subtree
				out.writeBits(1, 0);
			}
			count++;
			count += writeTree(current.getLeft(), write);
			count += writeTree(current.getRight(), write);
		}
		return count;
	}

	// write out the PseudoEOF encoding for this specific compression.
	private void writePEOF() {
		// the peof is the last index of the map
		String PEOF = map[IHuffConstants.ALPH_SIZE];
		for (int i = 0; i < PEOF.length(); i++) {
			out.writeBits(1, PEOF.charAt(i));
		}
	}


}
