/**
 * Main/launch program for Huff assignment. A better
 * comment than this is warranted.
 *
 */
public class Huff {

    public static void main(String[] args){
        IHuffViewer sv = new GUIHuffViewer("Huffman Compression");
        IHuffProcessor proc = new SimpleHuffProcessor();
        sv.setModel(proc);
    }
}
