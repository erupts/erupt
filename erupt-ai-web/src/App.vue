<script setup lang="ts">
import HelloWorld from './components/HelloWorld.vue'
import {ref} from "vue";

let a = ref<string>("1")
</script>

<template>
  <div>
    {{ a }}
  </div>
  <HelloWorld msg="Vite + Vue"/>
</template>

<style scoped>

</style>
