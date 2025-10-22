<template>
  <div>
    <h2>Available Icons Test</h2>
  </div>
</template>

<script setup lang="ts">
import * as ElementPlusIconsVue from '@element-plus/icons-vue';

// Log all available icon names to console
console.log('Available Element Plus Icons:', Object.keys(ElementPlusIconsVue));
</script>