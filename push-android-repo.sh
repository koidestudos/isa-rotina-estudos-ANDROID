#!/bin/bash
# Rode este script NO SEU COMPUTADOR (com acesso ao repo Android)
# para enviar o app nativo para koidestudos/isa-rotina-estudos-ANDROID

set -e

echo "📱 Enviando ISA Rotina Android para GitHub..."

if [ ! -d ".git" ]; then
  echo "Erro: rode dentro da pasta isa-rotina-estudos-ANDROID"
  exit 1
fi

git remote remove origin 2>/dev/null || true
git remote add origin https://github.com/koidestudos/isa-rotina-estudos-ANDROID.git

git push -u origin main --force

echo "✅ Pronto! Repositório atualizado:"
echo "   https://github.com/koidestudos/isa-rotina-estudos-ANDROID"
