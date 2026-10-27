package LowLevelDesign;
import java.util.*;
//product,cartItem,cart
class Product{
	String name;
	String category;
	Double price;
	
	
	public Product(String name, String category, Double price) {
		super();
		this.name = name;
		this.category = category;
		this.price = price;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public Double getPrice() {
		return price;
	}
	public void setPrice(Double price) {
		this.price = price;
	}
}
class CartItem{
	Product pd;
	int qty;
	CartItem(Product pd,int qty){
		this.pd=pd;
		this.qty=qty;
	}
	double getPrice(){
		return pd.getPrice();
	}
}
class Cart{
	List<CartItem> items=new ArrayList<>();
	Double totalamount=0.0;
	Double finalAmount=0.0;
	
	void addProduct(Product pd,int qty){
		CartItem cItem=new CartItem(pd,qty);
		items.add(cItem);
		this.totalamount+=pd.getPrice();
	}
	public void applyDiscount(double d) {
		this.totalamount-=d;
		if(this.totalamount<0) {
			this.totalamount=0.0;
		}
	}
}

//strategy
enum StrategyType {
    FLAT,
    PERCENT,
    PERCENT_WITH_CAP
}
class DiscountManager{
	public static DiscountManager instance=null;
	private DiscountManager() {
	}
	public static DiscountManager getInstance() {
		if(instance==null) return new DiscountManager();
		return instance;
	}
	
	public DiscountStrategy getStrategy(StrategyType type,double param1,double param2) {
		if(type==StrategyType.FLAT) {
			return new FlatStrategy(param1);
		}
		return null;
	}
}
interface DiscountStrategy{
	double calculate(double baseAmount);
}
class FlatStrategy implements DiscountStrategy{
	private double amount;
	FlatStrategy(double amount){
		this.amount=amount;
	}
	@Override
	public double calculate(double baseAmount) {
		return Math.min(amount, baseAmount);
	}
}
class PercentageStrategy implements DiscountStrategy{
	private double percentage=0.01;

	@Override
	public double calculate(double baseAmount) {
		return baseAmount*percentage;
	}
}

//coupon
abstract class Coupon{
	private Coupon next;
	
	public Coupon() {
		this.next=null;
	}
	public void setNext(Coupon nxt) {
		this.next=nxt;
	}
	public Coupon getNext() {
		return this.next;
	}
	public void applyCoupon(Cart c) {
		if(isApplicable(c)) {
			double discount=getDiscount(c);
			c.applyDiscount(discount);
			System.out.println(name() + " applied: " + discount);
		}
	}
	abstract boolean isApplicable(Cart c);
	abstract double getDiscount(Cart c);
	public abstract String name();
}
class BankCoupon extends Coupon{
	String bankName;
	private DiscountStrategy strat;
	BankCoupon(String bankName){
		this.bankName=bankName;
		this.strat=DiscountManager.getInstance().getStrategy(StrategyType.FLAT, 10.0, 0);
	}
	@Override
	boolean isApplicable(Cart c) {
		if(bankName.equals("HDFC")) return true;
		return false;
	}

	@Override
	double getDiscount(Cart c) {
		
		return strat.calculate(c.totalamount);
	}

	@Override
	public String name() {
		
		return "BankCoupon";
	}
}
class CouponManager{
	Coupon head=null;
	public static CouponManager manager=null;
	private CouponManager() {}
	
	public static CouponManager getInstance() {
		if(manager==null) return new CouponManager();
		return manager;
	}
	public void registerCoupon(Coupon c) {
		if(head==null) {
			head=c;
		}else {
			Coupon curr=head;
			while(curr.getNext()!=null) {
				curr=curr.getNext();
			}
			curr.setNext(c);
		}
	}
	public double applyAll(Cart cart) {
		if(head!=null) {
			head.applyCoupon(cart);
		}
		return cart.totalamount;
	}
}
public class DiscountCoupon {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		CouponManager mgr = CouponManager.getInstance();
		mgr.registerCoupon(new BankCoupon("HDFC"));
		
		 Product p1 = new Product("Winter Jacket", "Clothing", 1000.0);
	        Product p2 = new Product("Smartphone", "Electronics", 20000.0);
	        Product p3 = new Product("Jeans", "Clothing", 1000.0);
	        Product p4 = new Product("Headphones", "Electronics", 2000.0);

	        Cart cart = new Cart();
	        cart.addProduct(p1, 1);
	        cart.addProduct(p2, 1);
	        cart.addProduct(p3, 2);
	        cart.addProduct(p4, 1);
	        
	        double finalTotal = mgr.applyAll(cart);
	        System.out.println(finalTotal);

	}

}
