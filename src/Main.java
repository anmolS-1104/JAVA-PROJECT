import com.icrs.controller.ComplaintController;

public class Main {
    public static void main(String[] args) {

        ComplaintController controller = new ComplaintController();

        controller.addComplaint("payment failed");
        controller.addComplaint("delivery not received");

        controller.viewComplaints();
    }
}