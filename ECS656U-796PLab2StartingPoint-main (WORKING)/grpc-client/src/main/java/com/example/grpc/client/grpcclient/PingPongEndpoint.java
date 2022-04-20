package com.example.grpc.client.grpcclient;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.util.ArrayList;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.ExceptionHandler;

//import java.io.IOException;

@Controller
public class PingPongEndpoint {    

	GRPCClientService grpcClientService;    

	@Autowired
    	public PingPongEndpoint(GRPCClientService grpcClientService) {
        	this.grpcClientService = grpcClientService;
    	}    

    // root page which will display upload form to take matrix files
	@GetMapping("/")
		 public String UploadPage(Model model)
		 {
			 return "uploadform";
		 }

	// will run this code when /upload is entered on host server through POST. 
	// i.e. its the POST request handler
	// will either return uploaderror page or home page
	@PostMapping("/upload")
	public String upload(Model model, @RequestParam("files") MultipartFile[] files) throws IOException {
		try {
			// check if upload matrix files are empty
            if (files[0].isEmpty()||files[1].isEmpty()){
                model.addAttribute("error_message", "The files you have enteted are empty. Please upload correct ones.");
                return "uploaderror";
            }
			//check if uploaded files match the required amount (which has to be only 2 matrix files)
            else if (files.length > 2||files.length<2){
                model.addAttribute("error_message", "Please upload two matrix files.");
                return "uploaderror";
            }
            else{
				//pulling data from files
                byte[] matrix1 = files[0].getBytes();
				byte[] matrix2 = files[1].getBytes();

				//making String arrays to store the splitted rows
                String[] matrix1_rows = new String(matrix1).split("\n");
				String[] matrix2_rows = new String(matrix2).split("\n");
				
				//using int arraylists to store the columns 
                ArrayList<Integer> matrix1_cols = new ArrayList<Integer>();
				ArrayList<Integer> matrix2_cols = new ArrayList<Integer>();

				//adding in the matrix data to the arraylists
				for(int i = 0; i < matrix1_rows.length; i++){
					matrix1_cols.add(matrix1_rows[i].length() - matrix1_rows[i].replace(" ","").length());
				}
				for(int i = 0; i < matrix2_rows.length; i++){
					matrix2_cols.add(matrix2_rows[i].length() - matrix2_rows[i].replace(" ","").length());
				}

				//checking that the size of all the columns of both matrices are equal
                for(int i = 0; i < matrix1_cols.size(); i++)
				{
					if(matrix1_cols.get(i) != matrix1_cols.get(0)){
						model.addAttribute("error_message", "matrix 1 is not acceptable. Each column has to be of equal length.");
						return "uploaderror";
					}
				}

                for(int i = 0; i < matrix2_cols.size(); i++)
				{
					if(matrix2_cols.get(i) != matrix2_cols.get(0)){
						model.addAttribute("error_message", "matrix 2 is not acceptable. Each column has to be of equal length.");
						return "uploaderror";
					}
				}

				//checking that both matrices are square matrices.
				//a square matrix is when the num of columns = num of rows
                if (matrix1_rows.length != (matrix1_cols.get(0)+1)){
					model.addAttribute("error_message", "matrix 1 is not a square matrix");
                    //System.out.println(matrix1_cols.get(0));
					return "uploaderror";
				}

                if (matrix2_rows.length != (matrix2_cols.get(0)+1)){
					model.addAttribute("error_message", "matrix 2 is not a square matrix");
					return "uploaderror";
				}

				//checking that the dimensions of both matrices are equal
				//note as we have already checked for square matrices then we know rows length = cols length
                if (matrix1_rows.length != matrix2_rows.length){
					model.addAttribute("error_message", "The matrices you have uploaded are not compatiable");
					return "uploaderror";
				}
				
				//checking that the dimensions are a power of 2
				if (!(isPowerOfTwo(matrix1_rows.length))){
					model.addAttribute("error_message", "The dimensions of matrix 1 are not a power of two.");
					return "uploaderror";
				}

				if (!(isPowerOfTwo(matrix2_rows.length))){
					model.addAttribute("error_message", "The dimensions of matrix 2 are not a power of two.");
					return "uploaderror";
				}

            }

        }
        catch (Exception e){
			//catch any IO erros 
            model.addAttribute("error_message", "Input output error. Please enter valid matrix .txt files");
            return "uploaderror";
        }

		//this is the successful case and will return user to home 
        model.addAttribute("message", "Matrix files have been uploaded and checked successfully");
        return "home";
    }
	
	//function to check for power of two. 
	//source: https://www.geeksforgeeks.org/program-to-find-whether-a-given-number-is-power-of-2/
	public static boolean isPowerOfTwo(int n){
    	if(n==0)
    	return false;
 
		return (int)(Math.ceil((Math.log(n) / Math.log(2)))) == (int)(Math.floor(((Math.log(n) / Math.log(2)))));
	}

	@GetMapping("/ping")
    	public String ping() {
        	return grpcClientService.ping();
    	}

    //@GetMapping("/add")
	//	public String add(@RequestParam String matrix1, String matrix2) {
	//	return grpcClientService.add(matrix1,matrix2);
	//}

	//@GetMapping("/multiply")
		//public String multiply(@RequestParam String matrix1, String matrix2) {
		//return grpcClientService.multiply(matrix1,matrix2);
	//}
}
