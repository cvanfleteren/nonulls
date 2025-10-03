{
  description = "Dev shell with Maven and JDK 21 for this project";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-24.05";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, flake-utils }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = import nixpkgs { inherit system; };
      in {
        devShells.default = pkgs.mkShell {
          packages = with pkgs; [
            maven
            jdk21
            git
          ];
          shellHook = ''
            echo "Loaded dev shell with Maven and JDK."
            mvn -v || true
            java -version || true
          '';
        };
      });
}
