var TTCart = {
	load : function(){ // 加载购物车数据
		
	},
	init : function(){
		$("#toSettlement").click(function(){
			$("#cartForm").submit();
		});
		
		$("[name=checkItem]").click(function(){
			//计算总价
			TTCart.refreshTotalPrice();
			//将选中的商品填写到Form表单中
			var itemIds = [];
			$("[name=checkItem]:checked").each(function(i,e){
				itemIds.push($(e).val());
			})
			$("#cartForm [name=itemIds]").val(itemIds.join(","));
			
			//设置全选状态
			$("[name=toggle-checkboxes]").attr("checked",$("[name=checkItem]:not(:checked)").length==0);
		});
		
		//全选
		$("[name=toggle-checkboxes]").click(function(){
			$("[name=checkItem]").attr("checked",this.checked);
			TTCart.refreshTotalPrice();
//			if(this.checked){
//				$("[name=checkItem]:not(:checked)").click();
//			}else{
//				$("[name=checkItem]:checked").click();
//			}
		});
	},
	itemNumChange : function(){
		$(".increment").click(function(){//＋
			var _thisInput = $(this).siblings("input");
			_thisInput.val(eval(_thisInput.val()) + 1);
			$.post("/service/cart/update/"+_thisInput.attr("itemId")+"/"+_thisInput.val(),function(data){
				TTCart.refreshTotalPrice();
			});
		});
		$(".decrement").click(function(){//-
			var _thisInput = $(this).siblings("input");
			if(eval(_thisInput.val()) == 1){
				return ;
			}
			_thisInput.val(eval(_thisInput.val()) - 1);
			$.post("/service/cart/update/"+_thisInput.attr("itemId")+"/"+_thisInput.val(),function(data){
				TTCart.refreshTotalPrice();
			});
		});
		$(".quantity-form .quantity-text").rnumber(1);//限制只能输入数字
		//jQuery的change事件，文本框内容更变时执行
		$(".quantity-form .quantity-text").change(function(){
			var _thisInput = $(this);
			$.post("/service/cart/update/"+_thisInput.attr("itemId")+"/"+_thisInput.val(),function(data){
				TTCart.refreshTotalPrice();
			});
		});
	},
	refreshTotalPrice : function(){ //重新计算总价
		var total = 0;
		$("[name=checkItem]:checked").each(function(i,e){
			var _this = $(e);
			var _item =_this.parents("div.item").find(".quantity-form .quantity-text");
			total += (eval(_item.attr("itemPrice")) * 10000 * eval(_item.val())) / 10000;
		});
		$(".totalSkuPrice").html(new Number(total/100).toFixed(2)).priceFormat({ //价格格式化插件
			 prefix: '￥',
			 thousandsSeparator: ',',
			 centsLimit: 2
		});
	}
};

$(function(){
	TTCart.init();
	TTCart.load();
	TTCart.itemNumChange();
});