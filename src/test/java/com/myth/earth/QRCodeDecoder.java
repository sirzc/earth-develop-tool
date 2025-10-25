/*
 * Copyright (c) 2025 周潮. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.myth.earth;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class QRCodeDecoder {

    public static String readQRCode(File imageFile) throws IOException, NotFoundException {
        // 读取图像文件
        BufferedImage bufferedImage = ImageIO.read(imageFile);
        // 创建亮度源
        LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
        // 创建二值化位图
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
        // 解码获取结果
        Result result = new MultiFormatReader().decode(binaryBitmap);
        // 返回二维码内容
        return result.getText();
    }

    public static void main(String[] args) throws NotFoundException, IOException {
        File file = new File("/Users/zhouchao/Downloads/qrcode.png");
        System.out.println(readQRCode(file));
    }
}
