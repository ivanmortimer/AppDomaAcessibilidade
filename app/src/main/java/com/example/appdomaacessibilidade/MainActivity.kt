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
    private lateinit var tts: TextToSpeech // Variável do motor de voz

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicialização dos gerenciadores de áudio e do TTS
        audioHelper = AudioHelper(this)
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        tts = TextToSpeech(this, this)

        val listView: ListView = findViewById(R.id.listView)
        val buttonBluetooth: Button = findViewById(R.id.buttonBluetooth)

        // Preenchendo a ListView com funcionalidades
        val funcionalidadesDoma = arrayOf("Ler Mensagens", "Alertas de Segurança", "Instruções de Treinamento")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, funcionalidadesDoma)
        listView.adapter = adapter

        // PASSO 5 e 6: Reprodução de Áudio!
        // Quando o usuário clicar em um item da lista, o relógio falará o texto.
        listView.setOnItemClickListener { _, _, position, _ ->
            val textoParaFalar = funcionalidadesDoma[position]
            falarTexto(textoParaFalar)
        }

        // Detecção Dinâmica de Dispositivos de Áudio (mantida igual)
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

        // Facilitando a Conexão Bluetooth
        buttonBluetooth.setOnClickListener {
            // 1. Acessibilidade: Lê em voz alta o texto atual contido no botão
            falarTexto(buttonBluetooth.text.toString())

            // 2. Lógica original de conexão
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

                // Bônus de acessibilidade: se já estiver conectado, o relógio fala o aviso!
                falarTexto(aviso)
            }
        }
    }

    // Configuração do motor de voz
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // Tenta configurar o idioma para Português do Brasil
            val result = tts.setLanguage(Locale("pt", "BR"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "Idioma não suportado pelo TTS", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Falha ao inicializar o TTS", Toast.LENGTH_SHORT).show()
        }
    }

    // Função auxiliar para falar o texto
    private fun falarTexto(texto: String) {
        // Verifica se há alguma saída de áudio disponível (alto-falante ou bluetooth)
        val hasSpeaker = audioHelper.audioOutputAvailable(AudioDeviceInfo.TYPE_BUILTIN_SPEAKER)
        val hasBluetooth = audioHelper.audioOutputAvailable(AudioDeviceInfo.TYPE_BLUETOOTH_A2DP)

        if (hasSpeaker || hasBluetooth) {
            tts.speak(texto, TextToSpeech.QUEUE_FLUSH, null, "")
            Toast.makeText(this, "Reproduzindo: $texto", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Nenhuma saída de áudio detectada.", Toast.LENGTH_SHORT).show()
        }
    }

    // Libera a memória do TTS quando o app é fechado
    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
}