<template>
  <v-card>
    <v-card-title primary-title>
      <h3>添加构件</h3>
    </v-card-title>
    <v-card-text>
      <v-form v-model="valid">
        <v-text-field
          label="构件名"
          v-model="component.name"
          :rules="nameRules"
          :counter="nameCharLen"
          required
        ></v-text-field>
        <v-text-field multi-line
                      label="构件描述信息"
                      v-model="component.describe"
                      :rules="descRules"
                      required
        ></v-text-field>
      </v-form>
    </v-card-text>
    <v-card-actions>
      <v-spacer></v-spacer>
      <v-btn color="primary" :disabled="!valid" @click="addComponent">添加</v-btn>
    </v-card-actions>
  </v-card>
</template>

<script>
  import {COMP} from '../configs/srapp.api'

  export default {
    name: "compAdd",
    data() {
      return {
        valid: false,
        component: {
          name: '',
          describe: ''
        },
        nameCharLen: 25,
        nameRules: [
          v => !!v || '构件名不能为空',
          v => /^[A-Z_]+\.[A-Za-z_]+$/.test(v) || '构件名格式如：(ARRAYUTIL.join)',
          v => v.length <= this.nameCharLen || '构件名长度过长'
        ],
        descRules: [
          v => !!v || '构件描述不能为空'
        ]
      }
    },
    methods: {
      addComponent() {
        this.$http.post(COMP, this.component)
          .then(v => v.json().then(result => {
            if (result['numFound'] === 1) {
              this.$message.success("构件：" + this.component.name + "已添加成功！")
            }
          }), error => {
            if (error.status === 409) {
              this.$message.error("该构件已存在，不能重复添加！")
            }
          })
      }
    }
  }
</script>

<style scoped>

</style>
