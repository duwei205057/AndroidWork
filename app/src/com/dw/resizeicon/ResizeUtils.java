package com.dw.resizeicon;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by dw on 17-12-20.
 */

public class ResizeUtils {

    public static final String[] DEFAULT_TRANSFER_ICONS_RAW = {
            "back", "back_p", "backspace", "backspace_p", "logo",
            "bg", "bg1", "close" , "close_p" , "close_association" , "close_association_p", "candidate_back",
            "cloud_tip", "contact_sign", "down", "edit_copy", "edit_paste", "edit_paste_s", "edit_select_all", "edit_cut",
            "enter", "enter_p", "ime_function_edit_down", "ime_function_edit_down_s",
            "ime_function_edit_left", "ime_function_edit_left_s", "ime_function_edit_right",
            "ime_function_edit_right_s", "ime_function_edit_up", "ime_function_edit_up_s",
            "ime_switch_bihua", "ime_switch_bihua_p", "ime_switch_bihua_selected", "ime_switch_edit", "ime_switch_en26",
            "ime_switch_en26_p", "ime_switch_en26_selected", "ime_switch_en9", "ime_switch_en9_p", "ime_switch_en9_selected",
            "ime_switch_fanlingxi", "ime_switch_fanlingxi_selected", "ime_switch_float", "ime_switch_float_p", "ime_switch_game_keyboard",
            "ime_switch_game_keyboard_p", "ime_switch_game_keyboard_selected", "ime_switch_hw", "ime_switch_hw_p", "ime_switch_hw_selected",
            "ime_switch_more_settings", "ime_switch_more_settings_selected", "ime_switch_photograph_disabled", "ime_switch_photograph",
            "ime_switch_photograph_p", "ime_switch_photograph_selected", "ime_switch_pinyin26", "ime_switch_pinyin26_p", "ime_switch_pinyin26_selected",
            "ime_switch_pinyin9", "ime_switch_pinyin9_p", "ime_switch_pinyin9_selected", "ime_switch_p", "ime_switch_single_hand",
            "ime_switch_single_hand_selected", "ime_switch_wubi", "ime_switch_wubi_p", "ime_switch_wubi_selected",
            "lock", "lock_n", "lock_s", "lock_p", "lock_s_p", "more", "more_p", "no_back", "no_down",
            "no_more", "no_predict", "no_up", "predict", "predict_p", "predict_s", "predict_s_p",
            "separator", "separator_p", "separator_s", "shift", "shift_p", "shift_s", "shift_s_p",
            "space", "space_p", "space_digit", "space_digit_p", "space_hw", "space_hw_p", "space_voice", "space_voice_p", "space_voice_hw", "space_voice_hw_p",
            "up", "left", /*"right",*/ "candidate",
            "bg_edit_select", "bg_edit_select_p", "edit_delete", "edit_select", "edit_select_s",
            "settings_keyboard_feedback_vibration_down", "settings_keyboard_feedback_vibration_up",
            "settings_keyboard_feedback_volume_down", "settings_keyboard_feedback_volume_up", "settings_cht_switch", "settings_cht_switch_p",
            "settings_darkmode_switch", "settings_darkmode_switch_p", "settings_lingxi_switch", "settings_lingxi_switch_p", "settings_gamekeyboard_switch", "settings_gamekeyboard_switch_p",
            "settings_slidinput_switch", "settings_slidinput_switch_p", "settings_keyboard_adjust_enter", "settings_keyboard_feedback_enter", "settings_moreset_enter",
            "more_cands_bar_icon", "open", "correctmark", "correctmarkh", "delete", "exchange", "insert", "instead", "cloud_same", "cloud_correct", "clipboard_clear",
            "settings_fanlingxi_switch", "settings_fanlingxi_switch_p", "fanlingxi_mark", "fanlingxi_mark_h", "platform_add", "platform_apprecommend", "platform_barcode",
            "platform_camera", "platform_celldict", "platform_dimcode", "platform_hotdict", "platform_lbsdict", "platform_ocr", "platform_person_center", "platfrom_clipboard",
            "platform_shortcutphrases", "platform_textdirection", "platform_theme", "platform_transfer_pic", "platform_voice", "single_kb_change_disable_left", "single_kb_change_disable_right",
            "single_kb_change_normal_left", "single_kb_change_normal_right", "single_kb_reset_disable_left", "single_kb_reset_disable_right", "single_kb_reset_normal_left", "single_kb_reset_normal_right",
            "enterprise", "return_p", "return", "backspace_more_cand_p", "backspace_more_cand",
            "flx_triangle", "flx_triangle_bottom", "flx_triangle_top", "fanlingxi_mark", "fanlingxi_mark_h"
    };


    /**
     * 从/sdcard/res/压缩到sdcard/result/
     * @param themePath
     * @param iconnames
     * @param oldSize
     * @param size
     * @return
     * @throws FileNotFoundException
     */
    public static boolean resizeThemeNew(String themePath, String[] iconnames, int oldSize, int size) throws FileNotFoundException {
        long startTime = System.currentTimeMillis();
        themePath = "/sdcard/res";
        File sourceDir = new File(themePath);
        if(!sourceDir.exists()) sourceDir.mkdirs();
        if (oldSize <= size) return true;
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inTempStorage = new byte[16 * 1024];
        if (oldSize <= 0 || size <= 0) return false;
        Bitmap bitmap;
        for (String iconname : iconnames) {
            final String fileName = themePath + File.separator + iconname + ".png";
            final String targetName;
            targetName = "/sdcard" + File.separator + "result" + File.separator + iconname + ".png";
            File f = new File(fileName);
            if (!f.exists()) {
                continue;
            }
            if (f == null || f.length() == 0) return false;
            File targetDir = new File(targetName);
            if(!targetDir.getParentFile().exists()) targetDir.getParentFile().mkdirs();
            opts.inJustDecodeBounds = false;
//            opts.inSampleSize = (int) (1 / scale);
            opts.inDensity = oldSize;
            opts.inTargetDensity = size;
            bitmap =  BitmapFactory.decodeFile(fileName, opts);
            if (bitmap == null) return false;
            FileOutputStream fos = new FileOutputStream(targetDir);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
//            bitmap.recycle();
            bitmap = null;
            if (fos != null) {
                try {
                    fos.close();
                    fos = null;
                } catch (IOException e) {
//                Log.e(LOG_TAG, "Could not close zipfile", e);
                } catch (Exception e) {
                    // TODO: handle exception
                }
            }
        }
        return true;
    }
}
