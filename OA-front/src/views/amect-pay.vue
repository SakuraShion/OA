<template>
	<el-dialog title="缴纳罚款" :close-on-click-modal="false" v-model="visible" width="305px" center>
		<img :src="qrCode" class="qrcode" v-if="!result" />
		<div v-if="result" class="pay-success">
			<el-result icon="success" title="付款成功" subTitle="请根据提示进行操作"></el-result>
		</div>
		<div class="dialog-footer-style">
			<el-button type="danger" size="medium" v-if="!result" @click="cancelHandle">取消支付</el-button>
			<el-button type="primary" size="medium" v-if="!result" @click="successHandle">支付成功</el-button>
			<el-button type="primary" size="medium" v-if="result" @click="closeHandle">关闭窗口</el-button>
		</div>
	</el-dialog>
</template>

<script>
export default {
	data: function() {
		return {
			visible: false,
			dataForm: {
				id: null
			},
			result: false,
			qrCode: null
		};
	},
	methods: {
		init: function(id) {
		    let that = this;
		    that.visible = true;
		    that.dataForm.id = id;
		    that.result = false;
		    that.$nextTick(() => {
		        //从浏览器Cookie中获取Token令牌
		        let token = that.$cookies.get('token');
		        //向WebSocket服务类发送消息，让服务类缓存Session对象，可以推送消息给当前页面
		        that.$socket.sendObj({ opt: 'pay_amect', token: token });
		        //接收服务端推送的消息
		        that.$options.sockets.onmessage = function(resp) {
		            let data = resp.data;
		            if (data == '收款成功') {
		                that.result = true;
		            }
		        };
		        that.$http('amect/createNativeAmectPayOrder', 'POST', { amectId: id }, true, function(resp) {
		            that.qrCode = resp.qrCodeBase64;
		        });
		    });
		},
		
		cancelHandle: function() {
			this.visible = false;
		},
		
		successHandle: function() {
			let that = this;
			that.$http('amect/searchNativeAmectPayResult', 'POST', {amectId: that.dataForm.id}, true, function(resp) {
				let status = resp.status;
				that.$nextTick(() => {
					if(status == 1) {
						that.$message({
							message: '未缴纳罚款',
							type: 'warning',
							duration: 1200,
						});
					} else if(status == 2) {
						that.$message({
							message: '罚款缴纳成功',
							type: 'success',
							duration: 1200,
						});
						that.visible = false;
						that.$emit('refreshDataList');
					}
				});
			});
		},
		
		closeHandle: function() {
			this.visible = false;
			this.$emit('refreshDataList');
		},
	}
};
</script>

<style scoped>
.qrCode {
	width: 255px;
	height: 255px;
}
.pay-success {
	width: 255px;
	height: 255px;
}
.dialog-footer-style {
	padding-bottom: 30px;
	padding-top: 10px;
	text-align: center;
}
</style>
