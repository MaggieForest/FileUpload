import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @thing 实现客户端发送文件到服务器
 * @thing 客户端
 * @author zoushulin
 *
 */
public class Client {

	public final static int val = 2048;

	public static void main(String[] args) {
//		Client client = new Client();

		String path = null;// 本地文件夹地址
		String pathZip = null;// zip压缩包用于上传到服务器和备份到指定文件夹
		String serverIP = null;// 连接的服务器ip地址

		// 读取客户端文件，与项目存放在同一目录
		String[] clientProperties = readProperties();

		path = clientProperties[0];// 本地文件夹地址
		pathZip = clientProperties[1];// zip压缩包用于上传到服务器和备份到指定文件夹
		serverIP = clientProperties[2];// 连接的服务器ip地址

		try {
			
			while (true) {
				
				//检查本地上传文件夹内是否有文件
				File  new_file = new File(path);
				String[] tempList = new_file.list();// 取得当前目录下所有文件和文件夹列表
				if (tempList.length > 0) {
					handle(path, pathZip, serverIP);
					
				}
				Thread.sleep(1000*15); //15秒读取一次文件夹内容
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static String[] readProperties() {
		String pathname = "../ClientProperties.txt"; // 绝对路径或相对路径都可以，写入文件时演示相对路径,读取以上路径的input.txt文件
		String[] properties = (String[]) new String[3];// 用于存放服务端属性值
		// 防止文件建立或读取失败，用catch捕捉错误并打印，也可以throw;
		// 不关闭文件会导致资源的泄露，读写文件都同理
		// Java7的try-with-resources可以优雅关闭文件，异常时自动关闭文件；详细解读https://stackoverflow.com/a/12665271
		try (FileReader reader = new FileReader(pathname); BufferedReader br = new BufferedReader(reader) // 建立一个对象，它把文件内容转成计算机能读懂的语言
		) {
			String line;
			while ((line = br.readLine()) != null) {
				// 一次读入一行数据
				if (line.startsWith("path=")) {
					properties[0] = line.split("\"")[1];// 截取出本地文件夹地址
				} else if (line.startsWith("pathZip=")) {
					properties[1] = line.split("\"")[1];// 截取出上传后压缩包存放地址
				} else if (line.startsWith("serverIP=")) {
					properties[2] = line.split("\"")[1];// 截取出服务端ip地址
				} else {
					System.out.println("----properties----");
				}
			}
			for (String property : properties) {
				System.out.println(property);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return properties;
	}
	
	public static void handle(String path,String pathZip,String serverIP) throws Exception {
		// 文件打成zip压缩包，命名为当前时间
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");// 设置日期格式
		String date = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳
		System.out.println(date);
		String zipName = date;
		boolean flag = fileToZip(path, pathZip, zipName);
		if (flag) {
			System.out.println("文件打包成功!");
		} else {
			System.out.println("文件打包失败!");
		}

		// 传输压缩包
		// 获取文件夹内的压缩文件名
		// String zip_path_name = Client.getAllFileName(pathZip);
		String zip_path_name = pathZip + "\\" + date + ".zip";
		System.out.println(zip_path_name);
		try {
			sentZip(zip_path_name, serverIP);
		} catch (Throwable e) {
			e.printStackTrace();
		}

		// 清空上传文件夹,不要以 \ 结尾，只写到文件夹的名字
		delAllFile(path);
	}
	
	

	/**
	 *     获取一个文件夹下的压缩文件全路径     
	 * 
	 * @param path
	 *                
	 * @return listFileName
	 */
	public String getAllFileName(String path) {

		String ZipName = new String();

		File file = new File(path);
		ZipName = file.getName();

		if (ZipName != null) {
			System.err.println(ZipName + "****");
			return ZipName;
		} else {
			return "null";
		}
	}

	/**
	 * 成功传输的文件进行转移备份     
	 * 
	 * @param 当前文件的路径和文件名
	 *                
	 * @return void
	 */
	public static void copyFiletoSuccess(String currFile) {
		// 成功传输的文件进行转移

		File oldfile = new File(currFile);
		File newfile = new File(currFile.replace("uploadzip", "upload_backup"));
		if (!oldfile.exists() || !oldfile.isFile()) {
			System.out.println("复制文件为空");
			return;
		}
		if (newfile.exists()) {// 新文件路径下有同名文件直接覆盖
			newfile = new File(currFile.replace("uploadzip", "upload_backup"));
			try {
				newfile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			try {
				newfile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try {
			FileInputStream fin = new FileInputStream(oldfile);// 输入流
			try {
				FileOutputStream fout = new FileOutputStream(newfile, true);// 输出流
				byte[] b = new byte[1024];
				try {
					while ((fin.read(b)) != -1) {// 读取到末尾 返回-1 否则返回读取的字节个数
						fout.write(b);
					}
					fin.close();
					fout.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 传输压缩包   
	 * 
	 * @param 当前zip文件的全路径和serverIP
	 *                
	 * @return void
	 * @throws InterruptedException 
	 */
	public static void sentZip(String zip_path_name, String serverIP) throws InterruptedException {

		Socket socket;
		try {
			socket = new Socket(serverIP, 9191);
			// 准备两个流，文件输入流，socket输入流

			// System.out.println(listFileName.size());

			// 打印检查获取的文件名
			System.out.println(zip_path_name);

			// 本地的输入流
			FileInputStream fis = new FileInputStream(zip_path_name);
			// 把本地的输入流发出去
			OutputStream ous = socket.getOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(ous);

			System.out.println("开始读发送文件……！");
			// 先获取文件大小
			File file = new File(zip_path_name);
			long fileSize = file.length();
			byte[] bytes = new byte[val];

			// 算出即将发送字节数组的字数
			long times = fileSize / val + 1;
			// 算出最后一组字节数组的有效字节数
			int lastBytes = (int) fileSize % 2048;
			// 1.发送字节数组长度
			oos.writeInt(val);
			// 2.发送次数
			oos.writeLong(times);
			oos.flush();
			// 3.最后一次字节个数
			oos.writeInt(lastBytes);
			oos.flush();

			// 读取字节数组长度的字节，返回读取字节数中的数据个数
			int value = fis.read(bytes);
			while (value != -1) {
				// 偏移字节数读取
				oos.write(bytes, 0, value);
				oos.flush();
				value = fis.read(bytes);
			}
			System.out.println("文件发送完毕！");

			copyFiletoSuccess(zip_path_name);

			Thread.sleep(2000);
			// 关闭流
			fis.close();
			ous.close();
			socket.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 删除指定文件夹下的所有文件 
	 * @param path 目录
	 * @return boolen 是否成功
	 */
	public static boolean delAllFile(String path) {

		File file = new File(path);

		if (!file.exists()) {// 判断是否待删除目录是否存在
			System.err.println("The dir are not exists!");
			return false;
		}

		String[] tempList = file.list();// 取得当前目录下所有文件和文件夹

		for (String fileName : tempList) {
			System.gc(); // 加上确保文件能删除，不然可能删不掉
			File temp = new File(path + "\\" + fileName);
			if (temp.isDirectory()) {// 判断是否是目录
				delAllFile(temp.getAbsolutePath());// 递归调用，删除目录里的内容

				temp.delete();// 删除空目录
			} else {
				if (!temp.delete()) {// 直接删除文件
					System.err.println("Failed to delete " + fileName);
				}

			}
		}
		return true;
	}

	/**
	 * 将存放在sourceFilePath目录下的源文件，打包成fileName名称的zip文件，并存放到zipFilePath路径下
	 * 
	 * @param sourceFilePath
	 *            待压缩的文件路径
	 * @param zipFilePath
	 *            压缩后存放路径
	 * @param fileName
	 *            压缩后文件的名称
	 * @return
	 * @throws Exception
	 */
	public static boolean fileToZip(String sourceFilePath, String zipFilePath, String fileName) throws Exception {
		boolean flag = false;
		File sourceFile = new File(sourceFilePath);
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		FileOutputStream fos = null;
		ZipOutputStream zos = null;

		if (sourceFile.exists() == false) {
			System.out.println("待压缩的文件目录：" + sourceFilePath + "不存在.");
		} else {
			try {
				File zipFile = new File(zipFilePath + "/" + fileName + ".zip");
				if (zipFile.exists()) {
					System.out.println(zipFilePath + "目录下存在名字为:" + fileName + ".zip" + "打包文件.");
				} else {
					File[] sourceFiles = sourceFile.listFiles();
					if (null == sourceFiles || sourceFiles.length < 1) {
						System.out.println("待压缩的文件目录：" + sourceFilePath + "里面不存在文件，无需压缩.");
					} else {
						fos = new FileOutputStream(zipFile);
						zos = new ZipOutputStream(new BufferedOutputStream(fos));
						byte[] bufs = new byte[1024 * 10];
						for (int i = 0; i < sourceFiles.length; i++) {
							// 创建ZIP实体，并添加进压缩包
							ZipEntry zipEntry = new ZipEntry(sourceFiles[i].getName());
							zos.putNextEntry(zipEntry);
							// 读取待压缩的文件并写进压缩包里
							fis = new FileInputStream(sourceFiles[i]);
							bis = new BufferedInputStream(fis, 1024 * 10);
							int read = 0;
							while ((read = bis.read(bufs, 0, 1024 * 10)) != -1) {
								zos.write(bufs, 0, read);
							}
							flag = true;
						}
					}
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			} finally {
				// 关闭流
				zos.close();
				fos.close();
				bis.close();
				fis.close();
			}
		}
		return flag;
	}
}