package com.example.app.exception;

// 「MeshPolygonFactoryが扱えないmeshCodeが来た」という意味を持った例外
public class UnsupportedMeshCodeException extends RuntimeException {

	public UnsupportedMeshCodeException(String meshCode) {
		super("Unsupported mesh code: " + meshCode);
	}

	public UnsupportedMeshCodeException(int codeLength) {
		super("Unsupported mesh code Length: " + codeLength);
	}

}
