/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package Client;

import Client.ClientRunnable;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Scanner;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/*
This is the main code for the client.
The client requires three files:
1. This main file
2. One with the GUI
3. One with its Thread
The code performs local encryption on the client,
using a hash provided by the client (there's a default one).
The server receives the encrypted message and cannot decrypt it without the hash.
Other clients can only decrypt the message correctly if their hash
matches that of the client who sent the message.
Since the Netbeans-implemented classes for the GUI are private,
the entire flow is executed in the main, and the GUI is only used to access values,
or to update messages.
As they are private, public classes had to be created within the GUI file
that access the values we need from that file and pass them to us.
Sources:
    1. Basic chat: https://github.com/gyawaliamit7/MultiClientSocket 
    2. Encryption: https://reintech.io/blog/java-cryptography-encrypting-decrypting-data-tutorial
    3. String2SecretKey: https://www.baeldung.com/java-secret-key-to-string
 */
public class ClientMain {
    public static void main(String[] args) throws FileNotFoundException, IOException {
        // First, we create the GUI and make it visible
        ClientGUI jframe = new ClientGUI();
        jframe.setVisible(true);
        // Enter an infinite loop to execute the logic
        while (true) {
            // Constantly read the control value
            // This value will be activated for the first time when
            // we want to establish a connection
            int control = Integer.valueOf(jframe.control);
            // If a connection is requested, enter the if statement
            if (control == 1) {
                System.out.println(jframe.control);
                // The try-except block attempts to establish a connection with the server
                try (Socket socket = new Socket("localhost", 5000)) {
                    // Create an input object to read from the server
                    BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    // Create an object to send messages to the server
                    PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
                    // Create the Thread for the client
                    // This thread allows the client to constantly receive messages from the server
                    ClientRunnable clientRun = new ClientRunnable(socket);
                    // Start the Thread
                    new Thread(clientRun).start();
                    // Reset the control to 0
                    jframe.control = 0;
                    // Enter a new infinite loop
                    // From now on, the control variable will be used to send messages
                    while (true) {
                        // This infinite loop ensures that the connection is only made once
                        // And allows us to send as many messages as we want to the server
                        // The first thing we do is read the client's hash
                        // The client can update it during the connection if desired
                        String secretKeyString = jframe.return_stringKey();
                        SecretKey secretKey = convertStringToSecretKeyto(secretKeyString);
                        // Read our control variable
                        control = Integer.valueOf(jframe.control);
                        // If the control variable is 1, it means the client wants to send a message
                        if (control == 1) {
                            // Read the message
                            String message = jframe.return_message();
                            // Encrypt the message using the hash
                            String encrypted_message = encryptMessage(message, secretKey);
                            // Send the encrypted message to the server
                            output.println(encrypted_message);
                            // Reset the control variable
                            jframe.control = 0;
                        }
                        // Check if the server has sent any messages
                        String serverResponse = clientRun.response;
                        // If the message is not empty, enter the condition
                        if (!serverResponse.isEmpty()) {
                            // If we have received a message, decrypt it
                            String decryptedMessage = decryptMessage(serverResponse, secretKey);
                            // Write the message to the GUI
                            jframe.write_message(decryptedMessage);
                            // Set the response to empty
                            clientRun.response = "";
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Exception occurred in client main: " + e.getStackTrace());
                }
            }
        }
    }

    // We don't use this function, but it converts a SecretKey to a String
    public static String convertSecretKeyToString(SecretKey secretKey) throws NoSuchAlgorithmException {
        byte[] rawData = secretKey.getEncoded();
        String encodedKey = Base64.getEncoder().encodeToString(rawData);
        return encodedKey;
    }

    // This function receives a string and converts it to SecretKey
    public static SecretKey convertStringToSecretKeyto(String encodedKey) {
        // First, convert the String to bytes knowing that it is in base 64
        byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
        // Then convert it to SecretKey knowing that we use the AES algorithm for encryption
        SecretKey originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
        return originalKey;
    }

    // This function returns the encrypted message to be sent to the server
    public static String encryptMessage(String message, SecretKey secretKey)
            throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchAlgorithmException,
            NoSuchPaddingException {
        // Define the encryption algorithm to use
        String algorithm = "AES";
        String transformation = "AES/ECB/PKCS5Padding";
        // Create a cipher object with the key and encryption method to be used
        Cipher cipher = Cipher.getInstance(transformation);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        // Use the cipher object to encrypt the message

        System.out.println(message);
        // To encrypt it, it must first be converted to bytes
        byte[] encryptedBytes = cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));
        // Then convert the bytes to a string
        String encryptedBase64 = Base64.getEncoder().encodeToString(encryptedBytes);
        // And return the encrypted message
        return encryptedBase64;

    }

    // This function decrypts the message
    public static String decryptMessage(String encrypted_message, SecretKey secretKey)
            throws NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, InvalidKeyException,
            BadPaddingException {
        // Define the encryption algorithm to use
        String algorithm = "AES";
        String transformation = "AES/ECB/PKCS5Padding";
        // Create a cipher object with the key and decryption method to be used
        Cipher cipher = Cipher.getInstance(transformation);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        // Use the cipher object to decrypt the message

        // First, convert the encrypted message to bytes
        byte[] encryptedBytes = Base64.getDecoder().decode(encrypted_message);
        // Then decrypt the message, but it returns it in bytes
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        // Convert the bytes to String
        String decryptedText = new String(decryptedBytes, StandardCharsets.UTF_8);

        System.out.println("Decrypted data: " + decryptedText);
        // Return the decrypted message
        return decryptedText;
    }
}
