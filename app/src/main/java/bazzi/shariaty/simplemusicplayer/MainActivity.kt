package bazzi.shariaty.simplemusicplayer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.*
import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bazzi.shariaty.simplemusicplayer.ui.theme.SimpleMusicPlayerTheme

class MainActivity : ComponentActivity() {
    private lateinit var mediaPlayer: MediaPlayer
    val songs = listOf(
        SongInfo(R.raw.song1, "Erfan Tahmasbi - Parseh", R.drawable.song1_image),
        SongInfo(R.raw.song2, "Haamim - Raze Shab", R.drawable.song2_image),
        SongInfo(R.raw.song3, "Hojat Ashrafzadeh - Doret Begardam", R.drawable.song3_image),
        SongInfo(R.raw.song4, "Kasra Zahedi - Cheshmat", R.drawable.song4_image),
        SongInfo(R.raw.song5, "Mohsen Chavoshi - Mariz Hali", R.drawable.song5_image)
    )
    @Deprecated("Use currentSongIndexState instead", ReplaceWith("currentSongIndexState"))
    var currentSongIndex by mutableStateOf(0)
        private set

    val currentSongIndexState = mutableStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mediaPlayer = MediaPlayer.create(this, songs[currentSongIndexState.value].songId)
        setContent {
            SimpleMusicPlayerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MusicPlayer(mediaPlayer, this, songs) // Passing context and songs list to MusicPlayer
                }
            }
        }
    }
}


data class SongInfo(val songId: Int, val title: String, val imageId: Int)

@Composable
fun MusicPlayer(mediaPlayer: MediaPlayer, context: Context, songs: List<SongInfo>) {
    var isPlaying by remember { mutableStateOf(false) }
    val currentSongIndexState = remember { mutableStateOf(0) }
    val currentSong = songs[currentSongIndexState.value]

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = currentSong.imageId),
            contentDescription = null,
            modifier = Modifier.size(200.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = currentSong.title,
            fontSize = 24.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Seekbar(
            mediaPlayer = mediaPlayer,
            currentSongIndexState = currentSongIndexState,
            songs = songs,
        )
        Row(
            horizontalArrangement = Arrangement.Center
        ) {
            TextButton(
                onClick = { playNextSong(mediaPlayer, context, songs, currentSongIndexState) },
                modifier = Modifier.padding(6.dp)
            ) {
                Text("⏭️", fontSize = 30.sp)
            }

            TextButton(
                onClick = {
                    isPlaying = !isPlaying
                    if (isPlaying) {
                        mediaPlayer.start()
                    } else {
                        mediaPlayer.pause()
                    }
                },
                modifier = Modifier.padding(6.dp)
            ) {
                Text(if (isPlaying) "⏸️" else "▶️", fontSize = 30.sp)
            }

            TextButton(
                onClick = { playPreviousSong(mediaPlayer, context, songs, currentSongIndexState) },
                modifier = Modifier.padding(6.dp)
            ) {
                Text("⏮️", fontSize = 30.sp)
            }
        }
    }
}

@Composable
fun Seekbar(
    mediaPlayer: MediaPlayer,
    currentSongIndexState: MutableState<Int>,
    songs: List<SongInfo>
) {
    var progress by remember { mutableStateOf(0f) }

    DisposableEffect(Unit) {
        val handler = Handler()
        val updateProgress = object : Runnable {
            override fun run() {
                progress = mediaPlayer.currentPosition.toFloat()
                handler.postDelayed(this, 1000)
            }
        }
        handler.postDelayed(updateProgress, 1000)

        onDispose {
            handler.removeCallbacks(updateProgress)
        }
    }

    Slider(
        value = progress,
        onValueChange = { newValue ->
            mediaPlayer.seekTo(newValue.toInt())
        },
        valueRange = 0f..mediaPlayer.duration.toFloat(),
        onValueChangeFinished = {
            // No action needed here
        }
    )
}






fun playNextSong(
    mediaPlayer: MediaPlayer,
    context: Context,
    songs: List<SongInfo>,
    currentSongIndexState: MutableState<Int>
) {
    currentSongIndexState.value = (currentSongIndexState.value + 1) % songs.size
    mediaPlayer.reset()
    val nextSongId = songs[currentSongIndexState.value].songId
    val nextSongFd = context.resources.openRawResourceFd(nextSongId) // Calculating index for next song
    mediaPlayer.setDataSource(nextSongFd.fileDescriptor, nextSongFd.startOffset, nextSongFd.length) // Resetting the song from source
    mediaPlayer.prepare() // Preparing the media player
    mediaPlayer.start()
    nextSongFd.close() // Closing the file descriptor
}

fun playPreviousSong(
    mediaPlayer: MediaPlayer,
    context: Context,
    songs: List<SongInfo>,
    currentSongIndexState: MutableState<Int>
) {
    currentSongIndexState.value = (currentSongIndexState.value - 1 + songs.size) % songs.size
    mediaPlayer.reset()
    val previousSongId = songs[currentSongIndexState.value].songId
    val previousSongFd = context.resources.openRawResourceFd(previousSongId)
    mediaPlayer.setDataSource(previousSongFd.fileDescriptor, previousSongFd.startOffset, previousSongFd.length)
    mediaPlayer.prepare()
    mediaPlayer.start()
    previousSongFd.close()
}

/*@Composable
fun AnimatedImage(imageResId: Int, isVisible: Boolean) {
    AnimatedVisibility(visible = isVisible) {
        Image(
            painter = painterResource(id = imageResId),
            contentDescription = null,
            modifier = Modifier
                .size(200.dp)
                .padding(8.dp)
                .clip(RoundedCornerShape(8.dp))
        )
    }
}

*/

@Composable
fun Slider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChangeFinished: () -> Unit // تغییر اینجا به تابع بدون ورودی
) {
    androidx.compose.material3.Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = valueRange,
        onValueChangeFinished = onValueChangeFinished // استفاده از تابع بدون ورودی
    )
}
