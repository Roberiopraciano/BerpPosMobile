package br.com.berpsistemas.BerpPOSMobile

import android.content.Context
import android.util.Log
import com.zoop.pos.Zoop
import com.zoop.pos.Zoop.findPlugin
import com.zoop.pos.type.Environment
import com.zoop.pos.type.LogLevel
import com.zoop.pos.plugin.DashboardConfirmationResponse
import com.zoop.pos.plugin.smartpos.SmartPOSPlugin

class SmartPOSPluginManager(private val credentials: DashboardConfirmationResponse.Credentials? = null) {

    companion object {
        private const val TAG = "SmartPOSPluginManager"
    }

    fun initialize(context: Context) {
        try {
            Log.d(TAG, "Iniciando inicialização do SmartPOSPluginManager")

            // Verificar se kotlin-reflect está disponível antes de prosseguir
            checkKotlinReflect()

            // Inicializar Zoop
            Log.d(TAG, "Inicializando Zoop...")
            Zoop.initialize(context) {
                if (credentials != null) {
                    credentials {
                        marketplace = credentials.marketplace
                        seller = credentials.seller
                        accessKey = credentials.accessKey
                    }
                    Log.d(TAG, "Credenciais configuradas: marketplace=${credentials.marketplace}")
                }
            }

            // Configurar ambiente
            Zoop.setEnvironment(Environment.Production)
            Zoop.setLogLevel(LogLevel.Metric)
            Zoop.setStrict(false)
            Zoop.setTimeout(15 * 1000L)

            Log.d(TAG, "Configurações do Zoop aplicadas")

            // Verificar se já existe plugin antes de criar
            val existingPlugin = findPlugin<SmartPOSPlugin>()
            if (existingPlugin == null) {
                Log.d(TAG, "Criando SmartPOSPlugin...")

                try {
                    // Tentar criar o plugin com tratamento específico para kotlin-reflect
                    val constructorParams = Zoop.constructorParameters()
                    Log.d(TAG, "Parâmetros do construtor obtidos")

                    val plugin = SmartPOSPlugin(constructorParams)
                    Log.d(TAG, "SmartPOSPlugin criado com sucesso")

                    Zoop.plug(plugin)
                    Log.d(TAG, "SmartPOSPlugin plugado com sucesso")

                } catch (e: kotlin.jvm.KotlinReflectionNotSupportedError) {
                    Log.e(TAG, "Erro kotlin-reflect detectado ao criar SmartPOSPlugin")
                    throw KotlinReflectionException("kotlin-reflect não está disponível. Adicione a dependência no build.gradle.", e)
                } catch (e: NoClassDefFoundError) {
                    if (e.message?.contains("kotlin.reflect") == true) {
                        Log.e(TAG, "NoClassDefFoundError relacionado ao kotlin-reflect")
                        throw KotlinReflectionException("Classe kotlin-reflect não encontrada. Verifique as dependências.", e)
                    } else {
                        throw e
                    }
                }

            } else {
                Log.d(TAG, "SmartPOSPlugin já estava inicializado")
            }

            Log.d(TAG, "SmartPOSPluginManager inicializado com sucesso")

        } catch (e: KotlinReflectionException) {
            Log.e(TAG, "Erro de kotlin-reflect: ${e.message}")
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Erro geral ao inicializar SmartPOSPluginManager: ${e.message}", e)
            throw SmartPOSInitializationException("Falha na inicialização do SmartPOS: ${e.message}", e)
        }
    }

    private fun checkKotlinReflect() {
        try {
            // Tentar carregar classes essenciais do kotlin-reflect
            Class.forName("kotlin.reflect.KClass")
            Class.forName("kotlin.reflect.KProperty")
            Class.forName("kotlin.reflect.full.KClasses")
            Log.d(TAG, "kotlin-reflect verificado com sucesso")
        } catch (e: ClassNotFoundException) {
            val missingClass = e.message ?: "classe desconhecida"
            Log.e(TAG, "kotlin-reflect não encontrado: $missingClass")
            throw KotlinReflectionException(
                "kotlin-reflect não está no classpath. Classe faltante: $missingClass. " +
                        "Adicione 'implementation \"org.jetbrains.kotlin:kotlin-reflect:1.9.10\"' no build.gradle",
                e
            )
        }
    }

    fun terminate() {
        try {
            findPlugin<SmartPOSPlugin>()?.run {
                Log.d(TAG, "Finalizando SmartPOSPlugin...")
                Zoop.unplug(this)
                Log.d(TAG, "SmartPOSPlugin finalizado com sucesso")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao finalizar SmartPOSPlugin: ${e.message}", e)
        }
    }

    fun isPluginLoaded(): Boolean {
        return try {
            findPlugin<SmartPOSPlugin>() != null
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao verificar se plugin está carregado: ${e.message}")
            false
        }
    }
}

/**
 * Exceção específica para problemas com kotlin-reflect
 */
class KotlinReflectionException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Exceção específica para problemas de inicialização do SmartPOS
 */
class SmartPOSInitializationException(message: String, cause: Throwable? = null) : Exception(message, cause)