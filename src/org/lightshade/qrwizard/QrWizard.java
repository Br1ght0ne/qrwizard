package org.lightshade.qrwizard;

// БІБЛІОТЕКИ, НЕОБХІДНІ ДЛЯ РОБОТИ ПРОГРАМИ

// робота з зображеннями та стандартні бібліотеки

import java.awt.Color; // колір
import java.awt.Graphics2D; // 2d-графіка
import java.awt.image.BufferedImage; // оперування зображенням в пам'яті програми
import java.io.File; // робота з файлами
import java.io.IOException; // відлов помилок при роботі з файлами
import java.util.Date; // робота з датою та часом
import java.util.EnumMap; // об'єкти EnumMap та Map - для створення мапи кодування
import java.util.Map;
import java.util.Scanner; // робота з вхідними даними
import java.text.SimpleDateFormat; // форматування дати та часу
import javax.imageio.ImageIO; // розширення функціоналу java.io.File для роботи з зображеннями


// бібліотека ZXing (вільна ліцензія) для безпосереднього створення коду

import com.google.zxing.BarcodeFormat; // формат QR-коду
import com.google.zxing.EncodeHintType; // стандартні опції для мапи кодування
import com.google.zxing.WriterException; // відлов помилок при кодуванні тексту
import com.google.zxing.common.BitMatrix; // стандартний шаблон побітової матриці, адаптованої та розширеної для QR-кодування
import com.google.zxing.qrcode.QRCodeWriter; // безпосередньо об'єкт для проведення операції кодування 
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel; // встановленян рівня корекції помилок


// інтерфейс користувача на Swing, підлягає майбутній імплементації
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 * @author Олексій Філоненко
 * Оновлено: 20/09/2016
 */

// ПОЧАТОК ПРОГРАМИ
public class QrWizard {
	
	// об'єкт Scanner для одержання інформації від користувача
	private static Scanner user_input;

	public static void main(String[] args) {
		user_input = new Scanner( System.in ); // інстанціація Scanner
		
		// ОТРИМАННЯ ТЕКСТУ
		String myCodeText;
		System.out.print("Введіть текст для кодування: ");
		myCodeText = user_input.nextLine(); // зчитування тексту, доки користувач не натисне Enter
		
		// ОТРИМАННЯ ІМЕНІ ФАЙЛА
		String file_name;
		System.out.print("Введіть ім'я для файла (бажано коротке): ");
		file_name = user_input.next(); // зчитування одного слова
		
		// СТВОРЕННЯ ФАЙЛА
		Date date = new Date(); // створення об'єкту Date з поточною датою та часом
		SimpleDateFormat ft = new SimpleDateFormat ("-dd-MM-yyyy-hh-mm"); // форматування дати та часу
		
		/* ім'я файлу, складене з:
		 *   
		 *   1. Приставки 'QR-' для ідентифікації
		 *   2. Імені файла, введеного користувачем
		 *   3. Форматованої дати та часу
		 *   4. Розширення '.png'
		*/
		String filePath = "QR-" + file_name + ft.format(date) + ".png";
		int size = 250;
		String fileType = "png";
		File myFile = new File(filePath);
		
		// ОСНОВНА ПРОЦЕДУРА - ПОЧАТОК
		System.out.print("Створюємо Ваш код...");
		try {
			
			// створення об'єкту Map (мапи кодування), наданого бібліотекою ZXing
			Map<EncodeHintType, Object> hintMap = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
			hintMap.put(EncodeHintType.CHARACTER_SET, "UTF-8"); // вибір кодування, в нашому разі - Юнікод
			
			// вибір розміру рамки навколо коду, в нашомі разі - найменша (0)
			hintMap.put(EncodeHintType.MARGIN, 0);
			
			// вибір рівня корекції помилок, обрано стандартний - L
			hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
			
			// КОДУВАННЯ ТЕКСТУ В МАТРИЦЮ
			QRCodeWriter qrCodeWriter = new QRCodeWriter(); // створення об'єкту для запису матриці коду в зображення
			
			/*   створення, власне, матриці коду - побайтового представлення тексту. Аргументи:
			 * 
			 *   1.     Текст від користувача, що підлягає кодуванню
			 *   2.     Формат коду з бібліотеки BarcodeFormat
			 *   3, 4.  Розмір матриці (також - розмір самого зображення); оскільки код квадратний, аргументи рівні
			 *   5.     Створена нами мапа кодування, що має в собі деякі вищевказані налаштування 
		    */
			BitMatrix byteMatrix = qrCodeWriter.encode(myCodeText, BarcodeFormat.QR_CODE, size,
					size, hintMap);
			
			// СТВОРЕННЯ ОБ'ЄКТУ ЗОБРАЖЕННЯ
			int Width = byteMatrix.getWidth(); // розмір зображення дорівнює розміру матриці
			BufferedImage image = new BufferedImage(Width, Width,
				BufferedImage.TYPE_INT_RGB); // TYPE_INT_RGB - колірний тип зображення, в даному разі RGB (Red, Green, Blue)
			image.createGraphics(); // запис початкової графіки в об'єкт зображення
 
			// ЗАПИС МАТРИЦІ В ЗОБРАЖЕННЯ
			Graphics2D graphics = (Graphics2D) image.getGraphics(); // одержання графіки об'єкту
			graphics.setColor(Color.WHITE); // обрання білого кольору для запису
			graphics.fillRect(0, 0, Width, Width); // заповнення всього зображення білим кольором (створення фону) 
			graphics.setColor(Color.BLACK); // зміна кольору на чорний
			
			// двовимірний цикл, що замальовує частини зображення згідно до матриці
			for (int i = 0; i < Width; i++) {
				for (int j = 0; j < Width; j++) {
					if (byteMatrix.get(i, j)) {
						graphics.fillRect(i, j, 1, 1);
					}
				}
			}
			
			// ЗАПИС ЗОБРАЖЕННЯ В ФАЙЛ
			ImageIO.write(image, fileType, myFile);
			
		// БЛОК ДЛЯ ВІДЛОВУ МОЖЛИВИХ ПОМИЛОК
		} catch (WriterException e) { // можлива помилка при записі кодованого тексту в матрицю
			e.printStackTrace();
		} catch (IOException e) { // можлива помилка при одержанні/запису даних в файл
			e.printStackTrace();
		}
		System.out.println("\nКод успішно створений та знаходиться у файлі " + filePath + ".");
		// КІНЕЦЬ ПРОГРАМИ
	}
}