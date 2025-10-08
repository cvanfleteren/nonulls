{
  description = "Dev shell with Maven and JDK 21 for this project";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-25.05";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, flake-utils }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        starshipConfig = pkgs.writeTextFile {
           name = "starship.toml";
           destination = "/etc/starship.toml";
           text = builtins.readFile ./starship.toml; # path in your repo
        };
        pkgs = import nixpkgs { inherit system; };
        dockerTools = pkgs.dockerTools;
        # Build a root filesystem that links /bin so binaries are on PATH
        rootfs = pkgs.buildEnv {
          name = "dev-rootfs";
          paths = with pkgs; [ bash coreutils eza git lazygit fish starship maven jdk21 wget curl zellij zoxide];
          pathsToLink = [ "/bin" "/share" ];
        };
      in {
        devShells.default = pkgs.mkShell {
          packages = with pkgs; [
            maven
            jdk21
            git
            gnupg
          ];
          shellHook = ''
            echo "Loaded dev shell with Maven and JDK."
            mvn -v || true
            java -version || true
          '';
        };

        packages.dockerImage = dockerTools.buildLayeredImage {
          name = "nonulls/dev-fish";
          tag = "latest";
          contents = [ rootfs starshipConfig ];
          config = {
            User = "root"; # or "0:0" for uid:gid
            Env = [
              "LANG=C.UTF-8"
              "LC_ALL=C.UTF-8"
              "TERM=xterm-256color"
              "SHELL=/bin/fish"
              # Ensure the Nix-provided /bin is found
              "PATH=/bin:/usr/bin:/sbin:/usr/sbin"
              "STARSHIP_CONFIG=/etc/starship.toml"
            ];
            Entrypoint = [ "/bin/fish" "-l" ];
          };
          extraCommands = ''
             # Minimal passwd/group so USER=root resolves
            mkdir -p /etc
            echo 'root:x:0:0:root:/root:/bin/fish' > etc/passwd
            echo 'root:x:0:' > etc/group
            mkdir -p root

            # Configure fish + starship

            mkdir -p /etc/fish/conf.d
            cat > /etc/fish/conf.d/starship.fish <<'EOF'
            set -gx STARSHIP_LOG error
            # Initialize starship prompt for fish
            if type -q starship
              starship init fish | source
            end
            EOF
          '';
        };
      });
}
