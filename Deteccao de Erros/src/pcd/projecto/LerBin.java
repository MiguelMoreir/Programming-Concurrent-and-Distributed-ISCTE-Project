package pcd.projecto;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class LerBin {
	public static void main(String[] args) throws IOException {

		File input_file = new File ("data.bin");
		@SuppressWarnings("resource")
		DataInputStream data_in = new DataInputStream(new BufferedInputStream(new FileInputStream((input_file))));
		@SuppressWarnings("unused")
		byte [] array_of_ints = new byte [1000000000];
        int index = 0;

        long start = System.currentTimeMillis();

        while(true) {
            try {
                byte a = data_in.readByte();
                index++;
                System.out.println(a);
            }
            catch(EOFException eof) {
                System.out.println ("End of File");
                break;
            }
        }

        System.out.println(index);//posiçoes no array ocupados
        System.out.println(System.currentTimeMillis() - start);
    }
	}

