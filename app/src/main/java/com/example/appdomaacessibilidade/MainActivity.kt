package com.example.appdomaacessibilidade

import android.content.Context
import android.content.Intent
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Bundle
import android.provider.Settings
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var audioHelper: AudioHelper
    private lateinit var audioManager: AudioManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicialização dos gerenciadores de áudio
        audioHelper = AudioHelper(this)
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val listView: ListView = findViewById(R.id.listView)
        val buttonBluetooth: Button = findViewById(R.id.buttonBluetooth)

        // Preenchendo a ListView com funcionalidades de exemplo para a empresa Doma
        val funcionalidadesDoma = arrayOf("Ler Mensagens", "Alertas de Segurança", "Instruções de Treinamento")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, funcionalidadesDoma)
        listView.adapter = adapter

        // Detecção Dinâmica de Dispositivos de Áudio
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

        // Facilitando a Conexão Bluetooth pelo clique no botão
        buttonBluetooth.setOnClickListener {
            if (!audioHelper.audioOutputAvailable(AudioDeviceInfo.TYPE_BLUETOOTH_A2DP)) {
                val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    putExtra("EXTRA_CONNECTION_ONLY", true)
                    putExtra("EXTRA_CLOSE_ON_CONNECT", true)
                    putExtra("android.bluetooth.devicepicker.extra.FILTER_TYPE", 1)
                }
                startActivity(intent)
            } else {
                Toast.makeText(this, "Fone já está conectado.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}