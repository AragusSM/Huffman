import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
public class HuffDecompressor {

	private BitInputStream in;
	private BitOutputStream out;
	// headerformat is the format taken in from the file to recreate rebuilt, the
	// copy of the original tree, and bitsWritten is the number of bits written to
	// the output
	private int headerFormat;
	private HuffmanTree rebuilt;
	private int bitsWritten;

	// constructor given the input and another file to the output
	public HuffDecompressor(InputStream in, OutputStream out) {
		this.in = new BitInputStream(in);
		this.out = new BitOutputStream(out);
	}
	
	//code that decompresses the file
	//returns the number of bits written to the decompressed file.
	public int decompress() throws IOException {
		getFileFormat();
		if (headerFormat == IHuffConstants.STORE_TREE) {
			readTreeFormat();
		} else {
			readCountFormat();
		}
		writeBits();
		return bitsWritten;
	}

	// process the magic number
	public boolean isHuffFile() throws IOException {
		int magic = in.readBits(IHuffConstants.BITS_PER_INT);
		if (magic != IHuffConstants.MAGIC_NUMBER) {
			out.close();
			return false;
		}
		return true;
	}

	// process the header format
	private void getFileFormat() throws IOException {
		headerFormat = in.readBits(IHuffConstants.BITS_PER_INT);
	}

	// if the header is counts, call this method. creates an int array of the size
	// of the alphabet + 1 to include psuedo_EOF and then build a huffman tree from
	// this array
	private void readCountFormat() throws IOException {
		int[] freq = new int[IHuffConstants.ALPH_SIZE + 1];
		for (int i = 0; i < IHuffConstants.ALPH_SIZE; i++) {
			int inbits = in.readBits(IHuffConstants.BITS_PER_INT);
			freq[i] = inbits;
		}
		freq[IHuffConstants.ALPH_SIZE] = 1;
		rebuilt = new HuffmanTree(freq);
		rebuilt.recreateQueue();
		rebuilt.buildTree();
	}
	
	//if the header is tree, call this method, creates a new tree recursively using
	//the pre-order traversal information in the header.
	private void readTreeFormat() throws IOException {
		rebuilt = new HuffmanTree();
		rebuilt.recreateTree(in);
	}
	
	//once we finish building the tree, the following information will be actual data.
	//traverse through the tree over and over, writing the node's value in bits until 
	//the psuedo_EOF is reached.
	private void writeBits() throws IOException {
		boolean finished = false;
		TreeNode current = rebuilt.getRoot();
		while (!finished) {
			// current node is a leaf node.
			if (current.isLeaf()) {
				// if it is psuedo_EOF stop, otherwise write the value in bits per word and set
				// the current node to the root node.
				if (current.getValue() == IHuffConstants.PSEUDO_EOF) {
					finished = true;
				} else {
					out.writeBits(IHuffConstants.BITS_PER_WORD, current.getValue());
					bitsWritten += IHuffConstants.BITS_PER_WORD;
					current = rebuilt.getRoot();
				}
			} else {
				int direction = in.readBits(1);
				// go left
				if (direction == 0) {
					current = current.getLeft();
				} else {
					current = current.getRight();
				}
			}
		}
		out.close();
	}

}
