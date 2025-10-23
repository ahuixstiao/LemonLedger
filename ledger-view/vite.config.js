import {defineConfig,loadEnv} from 'vite'
import vue from '@vitejs/plugin-vue'
import {ElementPlusResolver, VantResolver} from 'unplugin-vue-components/resolvers'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'

export default defineConfig(({ command, mode }) => {
    const env = loadEnv(mode, process.cwd(), '')
    return{
        plugins: [vue(
        AutoImport({
            resolvers: [VantResolver(), ElementPlusResolver()],
        }),
        Components({
            resolvers: [VantResolver(), ElementPlusResolver()],
        }),
        )],
        base: '/',
        server: {
            port: 3000,
            open: true,
            host: true
        },
        define: {
        __APP_ENV__: env.APP_ENV
        },
    }
})

