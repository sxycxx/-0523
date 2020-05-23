var rejectType = 'prev';
var targetTask = '';
function onRejectPageLoaded(){
    //点击无线按钮 记录退回方式
    $('span[name="rejectType"]').click(function(){
        //记录值
        rejectType = $(this).attr('value');
        //显示选中
        $('span[name="rejectType"]').removeClass('oim-field_radio_status-checked');
        $(this).addClass('oim-field_radio_status-checked');

        //显示隐藏任务节点
        if(rejectType == 'appointo'){
            $("#targetTask" ).selectmenu( "widget" ).show();
        }else{
            $("#targetTask" ).selectmenu( "widget" ).hide();
            targetTask ='';
        }
    });
}

function onRejectBtnClick(){

	var reason = $('#_field_reject_reason').val();
	if(reason == null ||reason.length ==0){
			LUI.Message.info("信息","请输入退回原因!");
			return;
	}else{
			reason = reason.replace(/\r/g,"\\r").replace(/\n/g,"\\n").replace(/\"/g,'\\"').replace(/\'/g,"\\'").replace(/\\\\"/g,'\\"').replace(/\\\\'/g,"\\'");
	}
	if(rejectType == 'appointo' && targetTask == null){
	    LUI.Message.info("信息","请输入退回原因!");
		return;
	}
	//返回消息
	LUI.Page.closePage(true,{
		reason:reason,
		rejectType:rejectType,
		targetTask:targetTask
	});
}

//java数据源加载完成后 将结果显示到下拉框中
function onParentTaskDatasetLoaded(ds,ob,evt){
    $("#targetTask" ).empty();
    //增加一个新节点
    $("#targetTask" ).append('<option value ="">请选择退回对象...</option>');
    //增加所有已完成节点
    for(var i=0;i<ds.size();i++){
        var row = ds.getRecord(i);
        $("#targetTask" ).append('<option value ="'+row.getFieldValue('caoZuoDH')+'">'+row.getFieldValue('caoZuoMC')+'</option>');
    }
    
    //将任务节点seelect显示为jquery ui的下拉框
    $("#targetTask" ).selectmenu({
        change: function( event, data ) {
            targetTask = data.item.value;
        }
    }).selectmenu( "widget" ).addClass('reject-selectmenu-btn').hide();
    $("#targetTask-menu").addClass('reject-selectmenu-menu');
}


function onRejectCloseBtnClick(){
	//关闭窗口
	LUI.Page.closePage(false,{});
}