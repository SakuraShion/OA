<template>
	<el-dialog title="消息详情" v-model="visible" width="700px">
		<el-form>
			<el-row>
			  <el-col>
			    <el-form-item size="mini" style="text-align: left;">{{ form.msg }}</el-form-item>
			  </el-col>
			  <el-col :span="12">
			  </el-col>
			  <el-col :span="24">
			    <el-form-item></el-form-item>
			  </el-col>
			  <el-col :span="24">
			    <el-form-item></el-form-item>
			  </el-col>
			  <el-col :span="24">
			    <el-form-item></el-form-item>
			  </el-col>
			  <el-col :span="12">
			  </el-col>
			  <el-col :span="12">
			    <el-form-item style="color: #999; text-align: right;">{{ form.sendTime }}</el-form-item>
			  </el-col>
			</el-row>
		</el-form>
		<template #footer>
			<span class="dialog-footer">
				<el-button type="primary" size="medium" @click="visible = false">知道了</el-button>
			</span>
		</template>
	</el-dialog>
</template>

<script>
export default {
	data() {

		return {
			visible: false,
			// 表单参数
			form: {},
		};
	},
	methods: {
		init(row) {
			this.visible  = true;
			this.form = row;
		},
		
	}
};
</script>

<style></style>
