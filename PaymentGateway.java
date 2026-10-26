package LowLevelDesign;

import java.time.YearMonth;
import java.util.*;
//exceptions
class ValidationException extends PaymentException {
	ValidationException(String m) {
		super(m);
	}	
}
class PaymentException extends Exception { PaymentException(String m) { super(m); } }
class RetryableException extends Exception {
	public RetryableException(String message) {
		super(message);
	}
}
//payment Methods
enum PaymentType { CARD, UPI }
enum PaymentStatus{PROCESS,COMPLETED,PENDING,FAILED}
abstract class PaymentMethod{
	abstract void validate() throws ValidationException;
	abstract PaymentType getType();
}

class CardPayment extends PaymentMethod{
	private final String cardNum;
    private final String holder;
    private final YearMonth exp;
    private final String cvv;

    CardPayment(String cardNum, String holder, YearMonth exp, String cvv) {
        this.cardNum = cardNum;
        this.holder = holder;
        this.exp = exp;
        this.cvv = cvv;
    }
	@Override
	void validate() throws ValidationException {
		if (cardNum == null || !cardNum.matches("\\d{16}"))
            throw new ValidationException("Invalid card number");
        if (exp.isBefore(YearMonth.now()))
            throw new ValidationException("Card expired");
        if (cvv == null || !cvv.matches("\\d{3,4}"))
            throw new ValidationException("Invalid CVV");
	}

	@Override
	PaymentType getType() {
		// TODO Auto-generated method stub
		return PaymentType.CARD;
	}
	String getCardNumber() {return this.cardNum;}
}

//payment processors
interface IpaymentProcessor{
	void pay(Double amount,PaymentMethod method) throws Exception;
}
class CardProcessor implements IpaymentProcessor{
	BankService bank;
	CardProcessor(BankService bank) { this.bank = bank; }
	@Override
	public void pay(Double amount, PaymentMethod method) throws Exception{
		// TODO Auto-generated method stub
		CardPayment card = (CardPayment) method;
		bank.pay(amount, card.getCardNumber());
	}
}
class Proxy implements IpaymentProcessor{
	int maxRetries;
	IpaymentProcessor delegates;
	Proxy(IpaymentProcessor delegates,int retry){
		this.delegates=delegates;
		this.maxRetries=retry;
	}
	@Override
	public void pay(Double amount, PaymentMethod method) throws Exception{
		// TODO Auto-generated method stub
		for(int i=0;i<maxRetries;i++) {
			try { delegates.pay(amount, method); return; }
            catch (RetryableException e) { if (i >= maxRetries) throw e; }
		}
	}
}
//external Bank service
class BankService{
	void pay(Double amount,String resource) {
		System.out.println("amount "+amount +"Paid with resource ->"+resource);
	}
}

//paymentGateway 
class GateWay{
	Map<PaymentType,IpaymentProcessor>processors=new HashMap<>();
	
	GateWay(Map<PaymentType,IpaymentProcessor> processor){
		this.processors=processor;
	}
	Payment pay(Double amount,String orderId,PaymentMethod method,String idemPKey) throws Exception {
		Payment payment=new Payment(amount,orderId,method,idemPKey);
		try {
			System.out.println("Before validation .....");
            method.validate();
            processors.get(method.getType()).pay(amount, method);
            payment.setStatus(PaymentStatus.COMPLETED);
        } catch (PaymentException e) {
            payment.setStatus(PaymentStatus.FAILED);
            System.out.println("Exception due to " +e);
        }
		return payment;
	}
}
class Payment{
	String orderId;
	String idemKey;
	Double amount;
	PaymentMethod method;
	PaymentStatus status;
	public Payment(Double amount2, String orderId2, PaymentMethod method2, String idemPKey) {
		this.amount=amount2;
		this.orderId=orderId2;
		this.method=method2;
		this.idemKey=idemPKey;
	}
	void setStatus(PaymentStatus status) {
		this.status=status;
	}
}
public class PaymentGateway {

	public static void main(String[] args) {
		Map<PaymentType,IpaymentProcessor>processors=new HashMap<>();
		processors.put(PaymentType.CARD, new Proxy(new CardProcessor(new BankService()), 2));
		
		GateWay gateway=new GateWay(processors);
		
		try {
			gateway.pay(100.0, "ODR1", new CardPayment("10001","RAHUL",YearMonth.of(2028, 12),"199"), "IKEY");
		}catch(Exception ex) {
			System.out.println(ex);
		}
	}
}
