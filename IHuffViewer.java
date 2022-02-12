
public interface IHuffViewer {

    /**
     * Associates this view with the given model. The GUI/View will 
     * attach itself to the model so that communication between the view
     * and the model as well as <em>vice versa</em> is supported.
     * @param model is the model for this view
     */
    public void setModel(IHuffProcessor model);

    /**
     * To be called by model/client code to display strings in the View. Displays string
     * on a single line. Call multiple times with no interleaved clear to show several
     * strings.
     * @param s is string to be displayed
     */
    public void update(String s);

    /**
     * Display a text message in the view, 
     * thus a modeless message the user can ignore.
     * @param s is the message displayed
     */
    public void showMessage(String s);

    /**
     * Show a message indicating an error; 
     * @param s is the error-message displayed
     */
    public void showError(String s);
    
}
