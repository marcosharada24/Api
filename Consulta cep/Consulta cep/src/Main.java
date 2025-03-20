import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class ConsultaCEP {

    private static final String LOG_FILE = "log_consultas.txt";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int opcao;

        do {
            System.out.println("\nMenu:");
            System.out.println("1. Consultar CEP");
            System.out.println("2. Listar CEPs Consultados (Do log)");
            System.out.println("3. Sair");
            System.out.print("Escolha uma opção: ");
            opcao = scanner.nextInt();
            scanner.nextLine(); // Consumir nova linha

            switch (opcao) {
                case 1:
                    System.out.print("Digite o CEP: ");
                    String cep = scanner.nextLine().trim();
                    if (cep.matches("\\d{8}")) {
                        String endereco = consultarCEP(cep);
                        if (endereco != null) {
                            System.out.println("Endereço encontrado: " + endereco);
                            registrarLog(cep, endereco);
                        } else {
                            System.out.println("CEP inválido ou não encontrado.");
                        }
                    } else {
                        System.out.println("CEP inválido! Deve conter 8 dígitos.");
                    }
                    break;
                case 2:
                    listarLog();
                    break;
                case 3:
                    System.out.println("Saindo...");
                    break;
                default:
                    System.out.println("Opção inválida! Tente novamente.");
            }
        } while (opcao != 3);

        scanner.close();
    }

    private static String consultarCEP(String cep) {
        String urlString = "https://viacep.com.br/ws/" + cep + "/json/";

        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                return response.toString();
            } else {
                return null;
            }
        } catch (IOException e) {
            System.out.println("Erro ao conectar na API: " + e.getMessage());
            return null;
        }
    }

    private static void registrarLog(String cep, String endereco) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE, true))) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            writer.write(timestamp + " - CEP: " + cep + " - " + endereco);
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Erro ao escrever no log: " + e.getMessage());
        }
    }

    private static void listarLog() {
        try {
            if (Files.exists(Paths.get(LOG_FILE))) {
                System.out.println("\nCEPs Consultados:");
                Files.lines(Paths.get(LOG_FILE)).forEach(System.out::println);
            } else {
                System.out.println("Nenhum log encontrado.");
            }
        } catch (IOException e) {
            System.out.println("Erro ao ler o log: " + e.getMessage());
        }
    }
}
