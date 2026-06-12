package com.example.appdomaacessibilidade

import android.content.Context
import android.content.Intent
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Bundle
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var audioHelper: AudioHelper
    private lateinit var audioManager: AudioManager
    private lateinit var tts: TextToSpeech

    // Controle de estado para saber em qual tela o usuário está
    private var isMenuPrincipal = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        audioHelper = AudioHelper(this)
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        tts = TextToSpeech(this, this)

        val listView: ListView = findViewById(R.id.listView)
        val buttonBluetooth: Button = findViewById(R.id.buttonBluetooth)

        // Nossos dados (Mock)
        val menuPrincipal = arrayOf("Ler Mensagens", "Alertas de Segurança", "Instruções de Treinamento")
        val mensagensMock = arrayOf("← Voltar", "Mensagem 1: Reunião às 14 horas", "Mensagem 2: O seu pacote da Doma chegou")
        val alertasMock = arrayOf("← Voltar", "Alerta: Risco de chuva forte na região", "Alerta: Piso escorregadio no setor B")
        val instrucoesMock = arrayOf("← Voltar", "Passo 1: Desligue a energia da máquina", "Passo 2: Utilize sempre os óculos de proteção")

        // Carrega o Menu Principal ao iniciar
        val adapterMenu = ArrayAdapter(this, android.R.layout.simple_list_item_1, menuPrincipal)
        listView.adapter = adapterMenu

        // Lógica de cliques na lista
        listView.setOnItemClickListener { _, _, position, _ ->
            if (isMenuPrincipal) {
                // Estamos no Menu Principal: Lemos o texto e abrimos a sub-lista
                val textoClicado = menuPrincipal[position]
                falarTexto(textoClicado)

                val novoAdapter = when (position) {
                    0 -> ArrayAdapter(this, android.R.layout.simple_list_item_1, mensagensMock)
                    1 -> ArrayAdapter(this, android.R.layout.simple_list_item_1, alertasMock)
                    2 -> ArrayAdapter(this, android.R.layout.simple_list_item_1, instrucoesMock)
                    else -> adapterMenu
                }
                listView.adapter = novoAdapter
                isMenuPrincipal = false

            } else {
                // Estamos em uma Sub-lista
                val adapterAtual = listView.adapter as ArrayAdapter<String>
                val textoClicado = adapterAtual.getItem(position) ?: ""

                // TRATAMENTO DE ACESSIBILIDADE: Limpa o texto para a voz
                val textoParaFalar = if (textoClicado.contains("Voltar")) {
                    "Voltar"
                } else {
                    textoClicado
                }

                falarTexto(textoParaFalar)

                // Se o usuário clicou em Voltar, carregamos o Menu Principal novamente
                if (textoClicado == "← Voltar") {
                    listView.adapter = adapterMenu
                    isMenuPrincipal = true
                }
            }
        }

        // Restante do código intocado (Bluetooth e Callbacks)
        audioManager.registerAudioDeviceCallback(object : AudioDeviceCallback() {
            override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>?) {
                super.onAudioDevicesAdded(addedDevices)
                if (audioHelper.audioOutputAvailable(AudioDeviceInfo.TYPE_BLUETOOTH_A2DP)) {
                    Toast.makeText(this@MainActivity, "Fone Bluetooth conectado!", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>?) {
                super.onAudioDevicesRemoved(removedDevices)
                if (!audioHelper.audioOutputAvailable(AudioDeviceInfo.TYPE_BLUETOOTH_A2DP)) {
                    Toast.makeText(this@MainActivity, "Fone Bluetooth desconectado!", Toast.LENGTH_SHORT).show()
                }
            }
        }, null)

        buttonBluetooth.setOnClickListener {
            falarTexto(buttonBluetooth.text.toString())
            if (!audioHelper.audioOutputAvailable(AudioDeviceInfo.TYPE_BLUETOOTH_A2DP)) {
                val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    putExtra("EXTRA_CONNECTION_ONLY", true)
                    putExtra("EXTRA_CLOSE_ON_CONNECT", true)
                    putExtra("android.bluetooth.devicepicker.extra.FILTER_TYPE", 1)
                }
                startActivity(intent)
            } else {
                val aviso = "Fone já está conectado."
                Toast.makeText(this, aviso, Toast.LENGTH_SHORT).show()
                falarTexto(aviso)
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale("pt", "BR"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "Idioma não suportado pelo TTS", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Falha ao inicializar o TTS", Toast.LENGTH_SHORT).show()
        }
    }

    private fun falarTexto(texto: String) {
        val hasSpeaker = audioHelper.audioOutputAvailable(AudioDeviceInfo.TYPE_BUILTIN_SPEAKER)
        val hasBluetooth = audioHelper.audioOutputAvailable(AudioDeviceInfo.TYPE_BLUETOOTH_A2DP)

        if (hasSpeaker || hasBluetooth) {
            tts.speak(texto, TextToSpeech.QUEUE_FLUSH, null, "")
            Toast.makeText(this, "Reproduzindo áudio", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Nenhuma saída de áudio detectada.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
}