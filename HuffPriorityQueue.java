
import java.util.LinkedList;

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

/*
 * priority queue class that uses records the frequencies of the file.
 * the front of the queue is index of zero while the last element is size -1
 */
public class HuffPriorityQueue {

	private LinkedList<TreeNode> queue;
	private int size;

	public HuffPriorityQueue() {
		queue = new LinkedList<TreeNode>();
	}

	// pre: freq cannot be null
	// post: constructor that constructs priority queue based on an int array.
	public HuffPriorityQueue(int[] freq) {
		this();
		if (freq == null) {
			throw new IllegalArgumentException("array of frequencies cannot be null");
		}
		for (int i = 0; i < freq.length; i++) {
			TreeNode current = new TreeNode(i, freq[i]);
			//if frequency greater than 0, enqueue
			if(freq[i] > 0) {
				enqueue(current);
			}
		}
	}

	// pre: n cannot be null
	// post: adds the node to a proper position in the queue
	public void enqueue(TreeNode n) {
		if (n == null) {
			throw new IllegalArgumentException("node to enqueue cannot be null");
		}
		int index = 0;
		boolean positionFound = false;
		// get the position of the current node, if current is greater than the node,
		// add the node at current's position and shift everything
		while (index < queue.size() && !positionFound) {
			TreeNode current = queue.get(index);
			if (current.compareTo(n) > 0) {
				positionFound = true;
			} else {
				index++;
			}
		}
		size++;
		queue.add(index, n);
	}

	// pre: size cannot be <= 0
	// post: returns a tree node at the front of the queue
	public TreeNode dequeue() {
		if (size <= 0) {
			throw new IllegalStateException("the queue is currently empty");
		}
		size--;
		return queue.remove();

	}

	// get size of this queue. During deque loops, it is better to assign a variable
	// to the size so as to not modify the condition while removing.
	public int size() {
		return size;
	}
}
