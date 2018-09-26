<template>
  <v-card>
    <v-card-title primary-title>
      <h3>导入构件</h3>
    </v-card-title>
    <v-card-text>

      <el-upload
        class="upload-demo"
        name="files"
        accept="text/html"
        :action="action"
        :on-preview="handlePreview"
        :on-remove="handleRemove"
        :before-remove="beforeRemove"
        drag
        multiple
        :limit="3"
        :on-exceed="handleExceed"
        :file-list="fileList">
        <i class="el-icon-upload"></i>
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        <div slot="tip" class="el-upload__tip">只能上传html文件，总和不超过1500kb</div>
      </el-upload>
    </v-card-text>
  </v-card>
</template>

<script>
  import {COMP} from '../configs/srapp.api'
  export default {
    name: "compImport",
    data: () => ({
      action: COMP,
      fileList: []
    }),
    methods: {
      handleRemove(file, fileList) {
        console.log(file, fileList);
      },
      handlePreview(file) {
        console.log(file);
      },
      handleExceed(files, fileList) {
        this.$message.warning(`当前限制选择 3 个文件，本次选择了 ${files.length} 个文件，共选择了 ${files.length + fileList.length} 个文件`);
      },
      beforeRemove(file, fileList) {
        return this.$confirm(`确定移除 ${ file.name }？`);
      }
    }

  }
</script>

<style scoped>

</style>
