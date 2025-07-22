// utils/audioUtils.js
export const playBeep = (options = {}) => {
    const {
        frequency = 800,        // 频率 (Hz)
        duration = 1,         // 持续时间 (秒)
        volume = 0.3,           // 音量 (0-1)
        type = 'sine',          // 波形类型: 'sine', 'square', 'sawtooth', 'triangle'
        fadeOut = true,         // 是否淡出
        customSound = null      // 自定义音频文件路径
    } = options;

    // 如果提供了自定义音频文件
    if (customSound) {
        playCustomSound(customSound, volume);
        return;
    }

    // 播放默认生成的提示音
    try {
        const audioContext = new (window.AudioContext || window.webkitAudioContext)();
        const oscillator = audioContext.createOscillator();
        const gainNode = audioContext.createGain();

        oscillator.connect(gainNode);
        gainNode.connect(audioContext.destination);

        oscillator.frequency.setValueAtTime(frequency, audioContext.currentTime);
        oscillator.type = type;
        gainNode.gain.setValueAtTime(volume, audioContext.currentTime);

        if (fadeOut) {
            gainNode.gain.exponentialRampToValueAtTime(0.01, audioContext.currentTime + duration);
        }

        oscillator.start();
        oscillator.stop(audioContext.currentTime + duration);
    } catch (e) {
        console.warn('Audio not supported');
    }
};

// 播放自定义音频文件
const playCustomSound = (audioPath, volume = 0.3) => {
    try {
        const audio = new Audio(audioPath);
        audio.volume = volume;
        audio.play().catch(e => console.warn('Failed to play custom sound:', e));
    } catch (e) {
        console.warn('Failed to load custom sound:', e);
    }
};

// 预设的声音配置
export const SOUND_PRESETS = {
    default: { frequency: 800, duration: 0.3, volume: 0.3, type: 'sine' },
    gentle: { frequency: 600, duration: 0.5, volume: 0.2, type: 'sine' },
    sharp: { frequency: 1000, duration: 0.2, volume: 0.4, type: 'square' },
    soft: { frequency: 440, duration: 0.8, volume: 0.15, type: 'triangle' },
    notification: { frequency: 800, duration: 0.15, volume: 0.3, type: 'sine' }
};
